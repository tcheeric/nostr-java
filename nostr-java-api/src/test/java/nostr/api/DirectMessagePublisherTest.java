package nostr.api;

import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.client.relay.FakeRelay;
import nostr.client.relay.RelayPool;
import nostr.encryption.DirectMessageRelayLookup;
import nostr.encryption.Nip17DirectMessageService;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.DirectMessageRelayList;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies a NIP-17 message is delivered to each recipient's own relays, and that the sender
 * learns exactly who received it.
 */
class DirectMessagePublisherTest {

  private static final String SENDER_RELAY = "wss://relay.sender";
  private static final String ALICE_RELAY = "wss://relay.alice";
  private static final String BOB_RELAY = "wss://relay.bob";

  private final Map<String, FakeRelay> relays = new ConcurrentHashMap<>();
  private final Identity sender = Identity.generateRandomIdentity();

  // Verifies a message is published to the recipient's own nominated relay, since NIP-17 permits
  // delivery only there and not to the sender's relays.
  @Test
  void aMessageIsDeliveredToTheRecipientsOwnRelays() throws Exception {
    Identity alice = Identity.generateRandomIdentity();

    try (RelayPool pool = poolOf(SENDER_RELAY)) {
      List<RecipientDeliveryOutcome> outcomes =
          publisherFor(pool, relayListsOf(Map.of(alice.getPublicKey(), ALICE_RELAY)))
              .send(messageTo(alice.getPublicKey()));

      RecipientDeliveryOutcome toAlice = outcomeFor(outcomes, alice.getPublicKey());
      assertTrue(toAlice.isDelivered());
      assertEquals(List.of(ALICE_RELAY), toAlice.relays());
      assertEquals(1, relays.get(ALICE_RELAY).getSentMessages().size());
    }
  }

  // Verifies a recipient who published no relay list is reported unreachable rather than
  // silently skipped, so a sender can tell the user their message did not arrive.
  @Test
  void aRecipientWithNoRelayListIsReportedUnreachable() throws Exception {
    Identity silent = Identity.generateRandomIdentity();

    try (RelayPool pool = poolOf(SENDER_RELAY)) {
      List<RecipientDeliveryOutcome> outcomes =
          publisherFor(pool, owner -> Optional.empty()).send(messageTo(silent.getPublicKey()));

      RecipientDeliveryOutcome toSilent = outcomeFor(outcomes, silent.getPublicKey());
      assertFalse(toSilent.isDelivered());
      assertEquals(RecipientDeliveryOutcome.Status.UNREACHABLE, toSilent.status());
      assertTrue(toSilent.findReason().isPresent());
    }
  }

  // Verifies a group message reports each recipient separately, so partial delivery is visible
  // rather than collapsed into one verdict.
  @Test
  void aGroupMessageReportsEachRecipientSeparately() throws Exception {
    Identity alice = Identity.generateRandomIdentity();
    Identity bob = Identity.generateRandomIdentity();
    Identity silent = Identity.generateRandomIdentity();

    try (RelayPool pool = poolOf(SENDER_RELAY)) {
      List<RecipientDeliveryOutcome> outcomes =
          publisherFor(
                  pool,
                  relayListsOf(
                      Map.of(alice.getPublicKey(), ALICE_RELAY, bob.getPublicKey(), BOB_RELAY)))
              .send(
                  messageTo(alice.getPublicKey(), bob.getPublicKey(), silent.getPublicKey()));

      assertTrue(outcomeFor(outcomes, alice.getPublicKey()).isDelivered());
      assertTrue(outcomeFor(outcomes, bob.getPublicKey()).isDelivered());
      assertEquals(
          RecipientDeliveryOutcome.Status.UNREACHABLE,
          outcomeFor(outcomes, silent.getPublicKey()).status());
    }
  }

  // Verifies relays borrowed to reach a recipient are released afterwards, so a long-running
  // application does not accumulate a connection per person it has messaged.
  @Test
  void relaysBorrowedForADeliveryAreReleasedAfterwards() throws Exception {
    Identity alice = Identity.generateRandomIdentity();

    try (RelayPool pool = poolOf(SENDER_RELAY)) {
      publisherFor(pool, relayListsOf(Map.of(alice.getPublicKey(), ALICE_RELAY)))
          .send(messageTo(alice.getPublicKey()));

      assertEquals(List.of(SENDER_RELAY), pool.getRelays());
    }
  }

  // Verifies what was sent can be read back into the original message, so send and receive are
  // symmetrical through the same service.
  @Test
  void aSentMessageCanBeReadBack() throws Exception {
    Identity alice = Identity.generateRandomIdentity();

    try (RelayPool pool = poolOf(SENDER_RELAY)) {
      DirectMessagePublisher publisher =
          publisherFor(pool, relayListsOf(Map.of(alice.getPublicKey(), ALICE_RELAY)));
      publisher.send(messageTo(alice.getPublicKey()));

      EventMessage wrap = (EventMessage) relays.get(ALICE_RELAY).getSentMessages().getFirst();
      ChatMessage read =
          new DirectMessagePublisher(
                  new Nip17DirectMessageService(alice), owner -> Optional.empty(), pool)
              .read(wrap.getEvent());

      assertEquals("dinner at eight", read.getContent());
      assertEquals(sender.getPublicKey(), read.getSender());
    }
  }

  /**
   * Find one participant's outcome.
   *
   * <p>Every conversation includes the sender, because NIP-17 requires a copy addressed to them
   * as well, so outcomes are looked up by key rather than by position.
   */
  private RecipientDeliveryOutcome outcomeFor(
      List<RecipientDeliveryOutcome> outcomes, PublicKey participant) {
    return outcomes.stream()
        .filter(outcome -> outcome.recipient().equals(participant.toString()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No outcome reported for " + participant));
  }

  private DirectMessagePublisher publisherFor(RelayPool pool, DirectMessageRelayLookup lookup) {
    return new DirectMessagePublisher(new Nip17DirectMessageService(sender), lookup, pool);
  }

  private DirectMessageRelayLookup relayListsOf(Map<PublicKey, String> relayByOwner) {
    return owner ->
        Optional.ofNullable(relayByOwner.get(owner))
            .map(
                relayUri ->
                    new DirectMessageRelayList(
                        owner, List.of(new Relay(relayUri)), System.currentTimeMillis() / 1000));
  }

  private ChatMessage messageTo(PublicKey... recipients) {
    return ChatMessage.builder()
        .from(sender.getPublicKey())
        .to(List.of(recipients))
        .content("dinner at eight")
        .build();
  }

  private RelayPool poolOf(String... relayUris) {
    return new RelayPool(
        List.of(relayUris), relayUri -> relays.computeIfAbsent(relayUri, FakeRelay::accepting));
  }
}
