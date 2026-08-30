package nostr.encryption;

import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.Rumor;
import nostr.id.Identity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies sending and reading NIP-17 private direct messages.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
class Nip17DirectMessageServiceTest {

  /**
   * Keys from the worked example published in NIP-17, section "Examples", where Alice sends
   * "Hola, que tal?" to Bob. Given here in hex, since the spec quotes them as nsec.
   */
  private static final Identity ALICE =
      Identity.create("71f8de50a46c9996a21123280c6217c48f67d1378ff4fb14d4f7612181a1ebde");

  private static final Identity BOB =
      Identity.create("511cbb07ec2028bd2dcd039c447581a7f754df9d9a0e5c16b19a5422ab391563");

  private static Nip17DirectMessageService serviceFor(Identity identity) {
    return new Nip17DirectMessageService(identity);
  }

  /**
   * A message reaches its recipient with content, sender, and subject intact, which is the whole
   * point of the feature.
   */
  @Test
  @DisplayName("delivers a message to its recipient")
  void deliversMessageToRecipient() {
    ChatMessage sent =
        serviceFor(ALICE)
            .message()
            .to(BOB.getPublicKey())
            .subject("Dinner")
            .content("Hola, que tal?")
            .build();

    Map<PublicKey, GenericEvent> wraps = serviceFor(ALICE).composeByRecipient(sent);
    ChatMessage received = serviceFor(BOB).read(wraps.get(BOB.getPublicKey()));

    assertEquals("Hola, que tal?", received.getContent());
    assertEquals(ALICE.getPublicKey(), received.getSender());
    assertEquals("Dinner", received.getSubject().orElseThrow());
  }

  /**
   * A copy is addressed to the sender as well. Without it a sender could not read their own
   * conversation back, since they cannot decrypt a wrap addressed to someone else.
   */
  @Test
  @DisplayName("wraps a copy for the sender so they keep their own history")
  void wrapsCopyForSender() {
    ChatMessage sent =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("Hola, que tal?").build();

    Map<PublicKey, GenericEvent> wraps = serviceFor(ALICE).composeByRecipient(sent);

    assertTrue(wraps.containsKey(ALICE.getPublicKey()), "sender must receive their own copy");
    ChatMessage ownCopy = serviceFor(ALICE).read(wraps.get(ALICE.getPublicKey()));
    assertEquals("Hola, que tal?", ownCopy.getContent());
  }

  /** A two-party message produces exactly two events: one for the recipient, one for the sender. */
  @Test
  @DisplayName("produces one wrap per participant")
  void producesOneWrapPerParticipant() {
    ChatMessage sent =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("hello").build();

    List<GenericEvent> wraps = serviceFor(ALICE).compose(sent);

    assertEquals(2, wraps.size());
  }

  /** A group message reaches every participant, and each of them sees the whole recipient list. */
  @Test
  @DisplayName("delivers a group message to every participant")
  void deliversGroupMessage() {
    Identity carol = Identity.generateRandomIdentity();
    ChatMessage sent =
        serviceFor(ALICE)
            .message()
            .to(BOB.getPublicKey())
            .to(carol.getPublicKey())
            .content("dinner at eight?")
            .build();

    Map<PublicKey, GenericEvent> wraps = serviceFor(ALICE).composeByRecipient(sent);

    assertEquals(3, wraps.size());
    ChatMessage asBob = serviceFor(BOB).read(wraps.get(BOB.getPublicKey()));
    ChatMessage asCarol = serviceFor(carol).read(wraps.get(carol.getPublicKey()));
    assertEquals("dinner at eight?", asBob.getContent());
    assertEquals("dinner at eight?", asCarol.getContent());
    assertEquals(List.of(BOB.getPublicKey(), carol.getPublicKey()), asBob.getRecipients());
  }

  /**
   * Each participant's event is independently encrypted and signed, so an observer cannot tell
   * that two wraps belong to the same conversation.
   */
  @Test
  @DisplayName("makes each participant's wrap unlinkable to the others")
  void makesWrapsUnlinkable() {
    ChatMessage sent =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("hello").build();

    List<GenericEvent> wraps = serviceFor(ALICE).compose(sent);
    GenericEvent first = wraps.get(0);
    GenericEvent second = wraps.get(1);

    assertNotEquals(first.getPubKey(), second.getPubKey());
    assertNotEquals(first.getContent(), second.getContent());
    assertNotEquals(first.getId(), second.getId());
  }

  /** A participant cannot open a wrap addressed to a different participant. */
  @Test
  @DisplayName("keeps a wrap unreadable by anyone but its addressee")
  void keepsWrapUnreadableByOthers() {
    Identity eavesdropper = Identity.generateRandomIdentity();
    ChatMessage sent =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("private").build();

    GenericEvent bobsWrap =
        serviceFor(ALICE).composeByRecipient(sent).get(BOB.getPublicKey());

    assertThrows(GiftWrapException.class, () -> serviceFor(eavesdropper).read(bobsWrap));
  }

  /** A reply carries a reference to the message it answers. */
  @Test
  @DisplayName("preserves the parent reference on a reply")
  void preservesReplyReference() {
    ChatMessage original =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("dinner?").build();
    String parentId = original.toRumor().getId();

    ChatMessage reply =
        serviceFor(BOB)
            .message()
            .to(ALICE.getPublicKey())
            .inReplyTo(parentId)
            .content("yes, eight o'clock")
            .build();

    GenericEvent wrap = serviceFor(BOB).composeByRecipient(reply).get(ALICE.getPublicKey());
    ChatMessage received = serviceFor(ALICE).read(wrap);

    assertEquals(parentId, received.getReplyTo().orElseThrow());
  }

  /** A message with no subject reports none rather than an empty one. */
  @Test
  @DisplayName("reports no subject when none was set")
  void reportsNoSubjectWhenUnset() {
    ChatMessage sent =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("hello").build();

    GenericEvent wrap = serviceFor(ALICE).composeByRecipient(sent).get(BOB.getPublicKey());

    assertTrue(serviceFor(BOB).read(wrap).getSubject().isEmpty());
  }

  /** Content needing JSON escaping arrives intact. */
  @Test
  @DisplayName("delivers content that needs JSON escaping")
  void deliversAwkwardContent() {
    String awkward = "quote\" backslash\\ newline\n control\u0001 astral\uD83D\uDE80";
    ChatMessage sent =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content(awkward).build();

    GenericEvent wrap = serviceFor(ALICE).composeByRecipient(sent).get(BOB.getPublicKey());

    assertEquals(awkward, serviceFor(BOB).read(wrap).getContent());
  }

  /**
   * Sending a message attributed to another identity is refused, since the recipient would
   * reject it as forged. Failing here turns a confusing delivery failure into a clear error.
   */
  @Test
  @DisplayName("refuses to send a message authored by another identity")
  void refusesToSendOnBehalfOfAnother() {
    ChatMessage notMine =
        ChatMessage.builder()
            .from(BOB.getPublicKey())
            .to(ALICE.getPublicKey())
            .content("pretending to be Bob")
            .build();

    assertThrows(GiftWrapException.class, () -> serviceFor(ALICE).compose(notMine));
  }

  /** The published events never disclose the sender's key. */
  @Test
  @DisplayName("never exposes the sender on a published event")
  void neverExposesSender() {
    ChatMessage sent =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("hello").build();

    for (GenericEvent wrap : serviceFor(ALICE).compose(sent)) {
      assertNotEquals(ALICE.getPublicKey(), wrap.getPubKey());
      assertFalse(wrap.getContent().contains(ALICE.getPublicKey().toString()));
      assertEquals(Kinds.GIFT_WRAP, wrap.getKind());
    }
  }

  /** A message needs a recipient, since a conversation is defined by its participants. */
  @Test
  @DisplayName("refuses to build a message with no recipient")
  void refusesMessageWithoutRecipient() {
    ChatMessage.Builder builder = serviceFor(ALICE).message().content("into the void");

    assertThrows(IllegalStateException.class, builder::build);
  }

  /** Reading a wrap that carries something other than a chat message is refused. */
  @Test
  @DisplayName("refuses to read a wrap that is not a chat message")
  void refusesNonChatMessage() {
    Rumor note =
        Rumor.create(ALICE.getPublicKey(), Kinds.TEXT_NOTE, List.of(), "just a note");
    GenericEvent wrap = new Nip59GiftWrapper(ALICE).wrap(note, BOB.getPublicKey());

    assertThrows(IllegalArgumentException.class, () -> serviceFor(BOB).read(wrap));
  }

  /** A conversation survives a full exchange in both directions. */
  @Test
  @DisplayName("carries a conversation in both directions")
  void carriesConversationBothWays() {
    ChatMessage question =
        serviceFor(ALICE).message().to(BOB.getPublicKey()).content("Hola, que tal?").build();
    GenericEvent toBob = serviceFor(ALICE).composeByRecipient(question).get(BOB.getPublicKey());

    ChatMessage asRead = serviceFor(BOB).read(toBob);
    ChatMessage answer =
        serviceFor(BOB).message().to(asRead.getSender()).content("Muy bien, gracias").build();
    GenericEvent toAlice = serviceFor(BOB).composeByRecipient(answer).get(ALICE.getPublicKey());

    assertEquals("Muy bien, gracias", serviceFor(ALICE).read(toAlice).getContent());
    assertEquals(BOB.getPublicKey(), serviceFor(ALICE).read(toAlice).getSender());
  }
}
