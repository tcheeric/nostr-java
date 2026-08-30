package nostr.encryption;

import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.DirectMessageRelayList;
import nostr.id.Identity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that a message is routed only to the relays each recipient nominated.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
class MessageDeliveryPlanTest {

  private static final Identity ALICE =
      Identity.create("71f8de50a46c9996a21123280c6217c48f67d1378ff4fb14d4f7612181a1ebde");

  private static final Identity BOB =
      Identity.create("511cbb07ec2028bd2dcd039c447581a7f754df9d9a0e5c16b19a5422ab391563");

  private static final Relay ALICE_INBOX = new Relay("wss://alice.example");
  private static final Relay BOB_INBOX = new Relay("wss://inbox.nostr.wine");
  private static final Relay BOB_SECOND_INBOX = new Relay("wss://myrelay.nostr1.com");

  /** A lookup backed by a map, standing in for a relay query. */
  private static final class StubRelayLookup implements DirectMessageRelayLookup {

    private final Map<PublicKey, DirectMessageRelayList> lists = new HashMap<>();

    StubRelayLookup publishes(Identity owner, Relay... relays) {
      lists.put(
          owner.getPublicKey(),
          new DirectMessageRelayList(owner.getPublicKey(), List.of(relays), 1691518405L));
      return this;
    }

    @Override
    public Optional<DirectMessageRelayList> findFor(PublicKey owner) {
      return Optional.ofNullable(lists.get(owner));
    }
  }

  private static ChatMessage aMessageToBob() {
    return new Nip17DirectMessageService(ALICE)
        .message()
        .to(BOB.getPublicKey())
        .content("Hola, que tal?")
        .build();
  }

  private static MessageDelivery deliveryFor(List<MessageDelivery> plan, Identity recipient) {
    return plan.stream()
        .filter(delivery -> delivery.recipient().equals(recipient.getPublicKey()))
        .findFirst()
        .orElseThrow();
  }

  /** Each participant's copy is routed only to the relays that participant nominated. */
  @Test
  @DisplayName("routes each copy to its own recipient's relays")
  void routesToEachRecipientsOwnRelays() {
    StubRelayLookup relayLists =
        new StubRelayLookup()
            .publishes(ALICE, ALICE_INBOX)
            .publishes(BOB, BOB_INBOX, BOB_SECOND_INBOX);

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(aMessageToBob(), relayLists);

    assertEquals(List.of(BOB_INBOX, BOB_SECOND_INBOX), deliveryFor(plan, BOB).relays());
    assertEquals(List.of(ALICE_INBOX), deliveryFor(plan, ALICE).relays());
  }

  /** Every participant appears in the plan, including the sender's own copy. */
  @Test
  @DisplayName("plans a delivery for every participant")
  void plansDeliveryForEveryParticipant() {
    StubRelayLookup relayLists =
        new StubRelayLookup().publishes(ALICE, ALICE_INBOX).publishes(BOB, BOB_INBOX);

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(aMessageToBob(), relayLists);

    assertEquals(2, plan.size());
    assertTrue(plan.stream().allMatch(MessageDelivery::isDeliverable));
  }

  /**
   * A recipient who published no relay list is reported unreachable and no event is created for
   * them. NIP-17 forbids sending in this case, and a wrap that is never built cannot later be
   * published by mistake.
   */
  @Test
  @DisplayName("creates no event for a recipient who published no relay list")
  void createsNoEventForUnreachableRecipient() {
    StubRelayLookup relayLists = new StubRelayLookup().publishes(ALICE, ALICE_INBOX);

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(aMessageToBob(), relayLists);

    MessageDelivery toBob = deliveryFor(plan, BOB);
    assertFalse(toBob.isDeliverable());
    assertNull(toBob.giftWrap(), "no wrap may exist for a recipient we must not send to");
    assertTrue(toBob.relays().isEmpty());
  }

  /**
   * An unreachable recipient still appears in the plan. Omitting them would let a message go
   * partly undelivered without the caller ever noticing.
   */
  @Test
  @DisplayName("still reports an unreachable recipient")
  void stillReportsUnreachableRecipient() {
    StubRelayLookup relayLists = new StubRelayLookup().publishes(ALICE, ALICE_INBOX);

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(aMessageToBob(), relayLists);

    assertEquals(2, plan.size());
    assertEquals(BOB.getPublicKey(), deliveryFor(plan, BOB).recipient());
  }

  /** A recipient whose published list nominates no relay is unreachable, same as having none. */
  @Test
  @DisplayName("treats an empty relay list as unreachable")
  void treatsEmptyRelayListAsUnreachable() {
    StubRelayLookup relayLists =
        new StubRelayLookup().publishes(ALICE, ALICE_INBOX).publishes(BOB);

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(aMessageToBob(), relayLists);

    assertFalse(deliveryFor(plan, BOB).isDeliverable());
  }

  /** A deliverable copy carries an openable wrap addressed to that recipient. */
  @Test
  @DisplayName("produces a wrap the recipient can open")
  void producesOpenableWrap() {
    StubRelayLookup relayLists =
        new StubRelayLookup().publishes(ALICE, ALICE_INBOX).publishes(BOB, BOB_INBOX);

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(aMessageToBob(), relayLists);

    ChatMessage received =
        new Nip17DirectMessageService(BOB).read(deliveryFor(plan, BOB).giftWrap());

    assertEquals("Hola, que tal?", received.getContent());
    assertEquals(ALICE.getPublicKey(), received.getSender());
  }

  /** A group message routes each participant's copy independently. */
  @Test
  @DisplayName("routes a group message per participant")
  void routesGroupMessagePerParticipant() {
    Identity carol = Identity.generateRandomIdentity();
    Relay carolInbox = new Relay("wss://carol.example");
    StubRelayLookup relayLists =
        new StubRelayLookup()
            .publishes(ALICE, ALICE_INBOX)
            .publishes(BOB, BOB_INBOX)
            .publishes(carol, carolInbox);

    ChatMessage groupMessage =
        new Nip17DirectMessageService(ALICE)
            .message()
            .to(BOB.getPublicKey())
            .to(carol.getPublicKey())
            .content("dinner at eight?")
            .build();

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(groupMessage, relayLists);

    assertEquals(3, plan.size());
    assertEquals(List.of(carolInbox), deliveryFor(plan, carol).relays());
    assertEquals(List.of(BOB_INBOX), deliveryFor(plan, BOB).relays());
  }

  /** A sender who published no relay list keeps no copy, and the message still reaches others. */
  @Test
  @DisplayName("delivers to recipients even when the sender kept no relay list")
  void deliversWhenSenderHasNoRelayList() {
    StubRelayLookup relayLists = new StubRelayLookup().publishes(BOB, BOB_INBOX);

    List<MessageDelivery> plan =
        new Nip17DirectMessageService(ALICE).planDelivery(aMessageToBob(), relayLists);

    assertTrue(deliveryFor(plan, BOB).isDeliverable());
    assertFalse(deliveryFor(plan, ALICE).isDeliverable());
  }
}
