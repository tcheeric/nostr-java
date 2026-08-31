package nostr.api.integration;

import nostr.api.NostrClient;
import nostr.api.RecipientDeliveryOutcome;
import nostr.client.relay.PublishResult;
import nostr.event.filter.EventFilter;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the client against a real relay over a real WebSocket.
 *
 * <p>Everything else in this module is verified against scripted relay behaviour, which is the
 * right way to reproduce relays that disagree but never opens a socket. This test closes that
 * gap: it publishes an event and reads it back, so the encoding, the transport and the
 * subscription machinery are all shown to work against software that was not written to satisfy
 * these tests.
 */
@Testcontainers
class NostrClientRoundTripIT {

  private static final DockerImageName RELAY_IMAGE =
      DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13");
  private static final int RELAY_PORT = 8080;

  /** Retries for a relay whose startup panic leaves it accepting connections but inert. */
  private static final int RELAY_STARTUP_ATTEMPTS = 5;

  /**
   * The relay, held until it has proved it can store an event.
   *
   * <p>Neither the port nor the startup log is sufficient evidence of readiness. The port binds
   * before database migration finishes, and on some hardware a worker thread panics during
   * startup ({@code po2_denom was zero!}, a timing crate miscalibrating against the CPU clock)
   * after which the relay still accepts connections but silently answers nothing. Both produce a
   * publish that hangs until it times out, which looks exactly like a client bug.
   *
   * <p>The only dependable signal is the behaviour the tests need, so the wait publishes a
   * throwaway event and requires an {@code OK}. A relay left inert by the startup panic can never
   * satisfy that, so the container is started again rather than waited on indefinitely.
   */
  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(RELAY_IMAGE)
          .withExposedPorts(RELAY_PORT)
          .withStartupAttempts(RELAY_STARTUP_ATTEMPTS)
          .waitingFor(
              new RelayStoresEventsWaitStrategy()
                  .withStartupTimeout(Duration.ofSeconds(20)));

  // Verifies an event published through the client can be read back from the relay it was sent
  // to, proving encoding, transport and subscription work end to end.
  @Test
  void anEventPublishedIsReadBackFromTheRelay() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    String content = "round trip " + System.nanoTime();

    try (NostrClient nostr = NostrClient.builder().identity(author).relays(relayUri()).build()) {
      PublishResult published = nostr.publishTextNote(content);
      assertEquals(List.of(relayUri()), published.getAcceptingRelays());

      List<GenericEvent> received = new ArrayList<>();
      try (var subscription =
          nostr.subscribe(
              List.of(EventFilter.builder().author(author.getPublicKey().toString()).build()),
              received::add)) {

        await()
            .atMost(30, TimeUnit.SECONDS)
            .until(() -> received.stream().anyMatch(event -> content.equals(event.getContent())));
      }
    }
  }

  // Verifies a private direct message survives a real round trip: delivered to the recipient's
  // own relay, retrieved by them, and unwrapped back into the text that was sent.
  @Test
  void aDirectMessageIsDeliveredAndReadBack() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Identity recipient = Identity.generateRandomIdentity();
    String content = "private " + System.nanoTime();

    try (NostrClient recipientClient =
            NostrClient.builder().identity(recipient).relays(relayUri()).build();
        NostrClient senderClient =
            NostrClient.builder().identity(sender).relays(relayUri()).build()) {

      publishDirectMessageRelayList(recipientClient, recipient);
      awaitRelayListVisible(senderClient, recipient);

      List<RecipientDeliveryOutcome> outcomes =
          senderClient.sendDirectMessage(List.of(recipient.getPublicKey()), content);
      assertTrue(
          outcomes.stream().anyMatch(RecipientDeliveryOutcome::isDelivered),
          "no participant received the message: " + outcomes);

      AtomicBoolean readBack = new AtomicBoolean();
      try (var subscription =
          recipientClient.subscribe(
              List.of(EventFilter.builder().kind(GIFT_WRAP_KIND).build()),
              wrap -> readBackMatches(recipientClient, wrap, content, readBack))) {

        await().atMost(30, TimeUnit.SECONDS).until(readBack::get);
      }
    }
  }

  /**
   * Unwrap a received gift wrap, ignoring those addressed to somebody else.
   *
   * <p>A relay serves every wrap it holds, and only the ones sealed to this recipient can be
   * opened, so failing to unwrap is expected rather than a problem.
   */
  private void readBackMatches(
      NostrClient recipientClient, GenericEvent wrap, String content, AtomicBoolean readBack) {
    try {
      ChatMessage message = recipientClient.readDirectMessage(wrap);
      if (content.equals(message.getContent())) {
        readBack.set(true);
      }
    } catch (RuntimeException notForThisRecipient) {
      // Wraps addressed to others cannot be opened, which is the point of the scheme.
    }
  }

  /**
   * Wait until the sender can actually see the recipient's relay list.
   *
   * <p>Publishing returns once a relay accepts the event, which is not the same as the relay
   * having indexed it for queries. Sending before then would report the recipient unreachable
   * for a reason that says nothing about the code under test.
   */
  private void awaitRelayListVisible(NostrClient sender, Identity recipient) {
    await()
        .atMost(30, TimeUnit.SECONDS)
        .until(() -> sender.findDirectMessageRelays(recipient.getPublicKey()).isPresent());
  }

  private void publishDirectMessageRelayList(NostrClient client, Identity owner) throws Exception {
    client.publish(
        GenericEvent.builder()
            .pubKey(owner.getPublicKey())
            .kind(DIRECT_MESSAGE_RELAY_LIST_KIND)
            .content("")
            .tags(List.of(GenericTag.of("relay", relayUri())))
            .build());
  }

  private static String relayUri() {
    return "ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(RELAY_PORT);
  }

  private static final int GIFT_WRAP_KIND = 1059;
  private static final int DIRECT_MESSAGE_RELAY_LIST_KIND = 10050;
}
