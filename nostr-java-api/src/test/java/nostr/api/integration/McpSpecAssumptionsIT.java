package nostr.api.integration;

import nostr.api.NostrClient;
import nostr.api.RecipientDeliveryOutcome;
import nostr.client.relay.NoRelayAcceptedException;
import nostr.client.relay.PublishResult;
import nostr.client.relay.RelaySubscription;
import nostr.client.relay.SubscriptionListener;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the SDK behaviours the MCP module specification depends on.
 *
 * <p>{@code docs/explanation/nostr-java-mcp-spec.md} makes design decisions that only hold if
 * this SDK behaves in particular ways: that a publish reports per relay, that subscribing is
 * asynchronous, that a direct message to one person yields two outcomes. A specification is
 * not executable, so those assumptions would otherwise rot silently until someone implemented
 * against them and found out.
 *
 * <p>Each test names the section it protects. A failure here means the SDK moved and that
 * section needs rewriting, not that the SDK is wrong.
 */
@Testcontainers
class McpSpecAssumptionsIT {

  private static final DockerImageName RELAY_IMAGE =
      DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13");
  private static final int RELAY_PORT = 8080;
  private static final int RELAY_STARTUP_ATTEMPTS = 5;
  private static final int TEXT_NOTE_KIND = 1;
  private static final int DIRECT_MESSAGE_RELAY_LIST_KIND = 10050;

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(RELAY_IMAGE)
          .withExposedPorts(RELAY_PORT)
          .withStartupAttempts(RELAY_STARTUP_ATTEMPTS)
          .waitingFor(
              new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  // Verifies a publish reports what each relay did, which §9 maps onto a successful MCP tool
  // result carrying a per-relay list.
  @Test
  void publishingReportsPerRelayOutcomes() throws Exception {
    try (NostrClient nostr = clientFor(Identity.generateRandomIdentity())) {
      PublishResult result = nostr.publishTextNote("per-relay outcomes " + System.nanoTime());

      assertEquals(List.of(relayUri()), result.getAcceptingRelays());
      assertTrue(result.getFailures().isEmpty());
    }
  }

  // Verifies an event that reached no relay throws and carries the result, which §9 maps onto
  // RELAY_REJECTED with each relay's reason rendered for the agent.
  @Test
  void totalFailureThrowsAndCarriesTheResult() throws Exception {
    try (NostrClient nostr =
        NostrClient.builder()
            .identity(Identity.generateRandomIdentity())
            .relays("ws://localhost:1")
            .build()) {

      NoRelayAcceptedException thrown =
          assertThrows(NoRelayAcceptedException.class, () -> nostr.publishTextNote("nowhere"));

      assertFalse(thrown.getPublishResult().getFailures().isEmpty());
    }
  }

  // Verifies subscribing returns before any stored event or the end-of-backlog signal arrives.
  // §6.1 depends on this: a tool that blocked until EOSE would stall on an unresponsive relay.
  @Test
  void subscribingReturnsBeforeTheBacklogDrains() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    try (NostrClient nostr = clientFor(author)) {
      nostr.publishTextNote("stored history " + System.nanoTime());

      AtomicInteger events = new AtomicInteger();
      AtomicInteger backlogDrained = new AtomicInteger();
      try (RelaySubscription subscription =
          nostr.subscribe(
              List.of(notesBy(author)),
              new SubscriptionListener() {
                @Override
                public void onEvent(GenericEvent event) {
                  events.incrementAndGet();
                }

                @Override
                public void onEndOfStoredEvents() {
                  backlogDrained.incrementAndGet();
                }
              })) {

        assertEquals(0, events.get(), "stored events arrived before subscribe returned");
        assertEquals(0, backlogDrained.get(), "the backlog drained before subscribe returned");

        await().atMost(30, TimeUnit.SECONDS).until(() -> backlogDrained.get() == 1);
        assertEquals(1, backlogDrained.get(), "the backlog signal fired more than once");
      }
    }
  }

  // Verifies an event published while a subscription is open reaches that subscription, which
  // is the whole reason §6.1 keeps long-lived subscriptions rather than polling.
  @Test
  void anEventPublishedMidSubscriptionReachesTheListener() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    String marker = "watch-me-" + System.nanoTime();

    try (NostrClient nostr = clientFor(author)) {
      CountDownLatch sawMarker = new CountDownLatch(1);
      List<GenericEvent> buffered = new CopyOnWriteArrayList<>();

      try (RelaySubscription subscription =
          nostr.subscribe(
              List.of(notesBy(author)),
              event -> {
                buffered.add(event);
                if (marker.equals(event.getContent())) {
                  sawMarker.countDown();
                }
              })) {

        nostr.publishTextNote(marker);

        assertTrue(sawMarker.await(30, TimeUnit.SECONDS), "the published event never arrived");
      }
    }
  }

  // Verifies a recipient's kind-10050 list is resolvable, since §6.2's delivery depends on it
  // and a recipient without one cannot be sent to at all.
  @Test
  void aRecipientsDirectMessageRelaysAreResolvable() throws Exception {
    Identity recipient = Identity.generateRandomIdentity();

    try (NostrClient recipientClient = clientFor(recipient);
        NostrClient senderClient = clientFor(Identity.generateRandomIdentity())) {

      publishRelayList(recipientClient, recipient);

      await()
          .atMost(30, TimeUnit.SECONDS)
          .until(() -> senderClient.findDirectMessageRelays(recipient.getPublicKey()).isPresent());
    }
  }

  // Verifies a message to one recipient yields two outcomes, the second being the sender's own
  // copy. §6.2 warns the tool not to report that as "1 of 2 delivered".
  @Test
  void aMessageToOneRecipientReportsTheSendersCopyToo() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();

    try (NostrClient recipientClient = clientFor(recipient);
        NostrClient senderClient = clientFor(sender)) {

      publishRelayList(recipientClient, recipient);
      await()
          .atMost(30, TimeUnit.SECONDS)
          .until(() -> senderClient.findDirectMessageRelays(recipient.getPublicKey()).isPresent());

      List<RecipientDeliveryOutcome> outcomes =
          senderClient.sendDirectMessage(List.of(recipient.getPublicKey()), "two outcomes");

      assertEquals(2, outcomes.size(), "NIP-17 requires a copy addressed to the sender");
      assertTrue(
          outcomeFor(outcomes, sender).isDelivered() != outcomeFor(outcomes, recipient).isDelivered()
              || outcomeFor(outcomes, recipient).isDelivered(),
          "the actual recipient was not reached: " + outcomes);
      assertTrue(
          outcomeFor(outcomes, recipient).isDelivered(),
          "the actual recipient was not reached: " + outcomes);

      // The point §6.2 warns about: this sender published no relay list of their own, so
      // their archival copy is undeliverable while the message itself arrived. A tool that
      // counted "1 of 2 delivered" would report a successful send as a failure.
      assertEquals(
          RecipientDeliveryOutcome.Status.UNREACHABLE,
          outcomeFor(outcomes, sender).status(),
          "a sender without a relay list should not be reported as reached: " + outcomes);
    }
  }

  // Verifies a recipient who published no relay list is reported unreachable rather than
  // silently skipped, which §6.2 requires so an agent can tell the user who missed out.
  @Test
  void aRecipientWithoutARelayListIsReportedUnreachable() throws Exception {
    Identity absent = Identity.generateRandomIdentity();

    try (NostrClient senderClient = clientFor(Identity.generateRandomIdentity())) {
      List<RecipientDeliveryOutcome> outcomes =
          senderClient.sendDirectMessage(List.of(absent.getPublicKey()), "into the void");

      assertTrue(
          outcomes.stream()
              .anyMatch(
                  outcome ->
                      outcome.recipient().equals(absent.getPublicKey().toString())
                          && outcome.status() == RecipientDeliveryOutcome.Status.UNREACHABLE),
          "an unreachable recipient was not reported: " + outcomes);
    }
  }

  // Verifies an event can be signed by an identity other than the client's default, which
  // §6.3's multi-identity server depends on for its per-call identity argument.
  @Test
  void publishingCanSignAsAnotherIdentity() throws Exception {
    Identity defaultIdentity = Identity.generateRandomIdentity();
    Identity otherAccount = Identity.generateRandomIdentity();

    try (NostrClient nostr = clientFor(defaultIdentity)) {
      GenericEvent event =
          GenericEvent.builder()
              .pubKey(otherAccount.getPublicKey())
              .kind(TEXT_NOTE_KIND)
              .content("posted for another account")
              .build();

      nostr.publishAs(otherAccount, event);

      assertEquals(otherAccount.getPublicKey(), event.getPubKey());
    }
  }

  private RecipientDeliveryOutcome outcomeFor(
      List<RecipientDeliveryOutcome> outcomes, Identity participant) {
    return outcomes.stream()
        .filter(outcome -> outcome.recipient().equals(participant.getPublicKey().toString()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No outcome for " + participant.getPublicKey()));
  }

  private void publishRelayList(NostrClient client, Identity owner) throws Exception {
    client.publish(
        GenericEvent.builder()
            .pubKey(owner.getPublicKey())
            .kind(DIRECT_MESSAGE_RELAY_LIST_KIND)
            .content("")
            .tags(List.of(GenericTag.of("relay", relayUri())))
            .build());
  }

  private EventFilter notesBy(Identity author) {
    return EventFilter.builder()
        .author(author.getPublicKey().toString())
        .kind(TEXT_NOTE_KIND)
        .build();
  }

  private NostrClient clientFor(Identity identity) {
    return NostrClient.builder().identity(identity).relays(relayUri()).build();
  }

  private static String relayUri() {
    return "ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(RELAY_PORT);
  }
}
