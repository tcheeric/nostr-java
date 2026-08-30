package nostr.encryption;

import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.DirectMessageRelayList;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compiles and runs the examples printed in {@code docs/howto/private-direct-messages.md}.
 *
 * <p>Documentation that has drifted from the API is worse than none, so each snippet in the
 * guide appears here in a form close enough to fail this test if the API changes under it.
 */
class PrivateDirectMessagesHowToTest {

  private static final Identity ALICE =
      Identity.create("71f8de50a46c9996a21123280c6217c48f67d1378ff4fb14d4f7612181a1ebde");

  private static final Identity BOB =
      Identity.create("511cbb07ec2028bd2dcd039c447581a7f754df9d9a0e5c16b19a5422ab391563");

  /** The "Publish where you receive messages" snippet. */
  @Test
  @DisplayName("publishes a kind-10050 inbox list as the guide shows")
  void publishesInboxList() {
    Identity alice = ALICE;

    DirectMessageRelayList inbox =
        new DirectMessageRelayList(
            alice.getPublicKey(),
            List.of(new Relay("wss://inbox.nostr.wine")),
            Instant.now().getEpochSecond());

    GenericEvent inboxEvent = inbox.toEvent();
    alice.sign(inboxEvent);

    assertEquals(Kinds.DM_RELAY_LIST, inboxEvent.getKind());
    assertTrue(inboxEvent.isSigned());
  }

  /** The "Send a message" snippet. */
  @Test
  @DisplayName("composes a message as the guide shows")
  void composesMessage() {
    PublicKey bobPublicKey = BOB.getPublicKey();
    Nip17DirectMessageService messages = new Nip17DirectMessageService(ALICE);

    ChatMessage message =
        messages
            .message()
            .to(bobPublicKey)
            .subject("Dinner")
            .content("Are you going to the party tonight?")
            .build();

    List<GenericEvent> wraps = messages.compose(message);

    assertEquals(2, wraps.size(), "one wrap per participant, including the sender's own copy");
  }

  /** The "Send to the right relays" snippet. */
  @Test
  @DisplayName("plans delivery as the guide shows")
  void plansDelivery() {
    Relay bobInbox = new Relay("wss://inbox.nostr.wine");
    DirectMessageRelayLookup relayLists =
        pubkey ->
            pubkey.equals(BOB.getPublicKey())
                ? Optional.of(
                    new DirectMessageRelayList(pubkey, List.of(bobInbox), Instant.now().getEpochSecond()))
                : Optional.empty();

    Nip17DirectMessageService messages = new Nip17DirectMessageService(ALICE);
    ChatMessage message = messages.message().to(BOB.getPublicKey()).content("hello").build();

    List<PublicKey> unreachable = new ArrayList<>();
    List<GenericEvent> published = new ArrayList<>();
    for (MessageDelivery delivery : messages.planDelivery(message, relayLists)) {
      if (delivery.isDeliverable()) {
        published.add(delivery.giftWrap());
        assertEquals(List.of(bobInbox), delivery.relays());
      } else {
        unreachable.add(delivery.recipient());
      }
    }

    assertEquals(1, published.size());
    assertEquals(List.of(ALICE.getPublicKey()), unreachable);
  }

  /** The "Read your messages" snippet, including the skip-on-failure loop. */
  @Test
  @DisplayName("reads an inbox as the guide shows, skipping wraps it cannot open")
  void readsInboxSkippingUnopenableWraps() {
    Nip17DirectMessageService fromAlice = new Nip17DirectMessageService(ALICE);
    ChatMessage sent = fromAlice.message().to(BOB.getPublicKey()).content("Hola, que tal?").build();

    Identity stranger = Identity.generateRandomIdentity();
    GenericEvent notForBob =
        new Nip17DirectMessageService(stranger)
            .composeByRecipient(
                new Nip17DirectMessageService(stranger)
                    .message()
                    .to(stranger.getPublicKey())
                    .content("someone else's business")
                    .build())
            .get(stranger.getPublicKey());

    List<GenericEvent> incomingEvents =
        List.of(notForBob, fromAlice.composeByRecipient(sent).get(BOB.getPublicKey()));

    Nip17DirectMessageService messages = new Nip17DirectMessageService(BOB);
    List<String> read = new ArrayList<>();
    for (GenericEvent giftWrap : incomingEvents) {
      try {
        ChatMessage received = messages.read(giftWrap);
        read.add(received.getContent());
      } catch (GiftWrapException notForUs) {
        // Expected: a kind-1059 subscription also delivers wraps we cannot open.
      }
    }

    assertEquals(List.of("Hola, que tal?"), read);
  }

  /** The "Reply to a message" snippet. */
  @Test
  @DisplayName("replies as the guide shows")
  void replies() {
    Nip17DirectMessageService messages = new Nip17DirectMessageService(BOB);
    ChatMessage original =
        new Nip17DirectMessageService(ALICE)
            .message()
            .to(BOB.getPublicKey())
            .content("dinner?")
            .build();
    GenericEvent wrap =
        new Nip17DirectMessageService(ALICE).composeByRecipient(original).get(BOB.getPublicKey());

    ChatMessage received = messages.read(wrap);
    String receivedEventId = original.toRumor().getId();

    ChatMessage reply =
        messages
            .message()
            .to(received.getSender())
            .inReplyTo(receivedEventId)
            .content("Yes, see you at eight")
            .build();

    assertEquals(receivedEventId, reply.getReplyTo().orElseThrow());
  }

  /** The "Group conversations" snippet. */
  @Test
  @DisplayName("sends a group message as the guide shows")
  void sendsGroupMessage() {
    PublicKey carolPublicKey = Identity.generateRandomIdentity().getPublicKey();
    Nip17DirectMessageService messages = new Nip17DirectMessageService(ALICE);

    ChatMessage groupMessage =
        messages
            .message()
            .to(BOB.getPublicKey())
            .to(carolPublicKey)
            .content("Dinner at eight?")
            .build();

    assertEquals(3, messages.compose(groupMessage).size());
  }

  /** The "Ephemeral messages" snippet. */
  @Test
  @DisplayName("builds an ephemeral chat service as the guide shows")
  void buildsEphemeralChatService() {
    Identity alice = ALICE;

    DirectMessageService liveChat =
        new Nip17DirectMessageService(alice, new Nip59GiftWrapper(alice, Kinds.EPHEMERAL_GIFT_WRAP));

    ChatMessage message =
        ChatMessage.builder()
            .from(alice.getPublicKey())
            .to(BOB.getPublicKey())
            .content("are you there?")
            .build();

    assertTrue(
        liveChat.compose(message).stream()
            .allMatch(wrap -> Kinds.EPHEMERAL_GIFT_WRAP == wrap.getKind()));
  }
}
