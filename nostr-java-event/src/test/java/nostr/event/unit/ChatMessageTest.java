package nostr.event.unit;

import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.Rumor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the NIP-17 chat message and its translation to and from a rumor.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/17.md">NIP-17</a>
 */
class ChatMessageTest {

  private static final PublicKey ALICE =
      new PublicKey("611df01bfcf85c26ae65453b772d8f1dfd25c264621c0277e1fc1518686faef9");

  private static final PublicKey BOB =
      new PublicKey("918e2da906df4ccd12c8ac672d8335add131a4cf9d27ce42b3bb3625755f0788");

  private static final PublicKey CAROL =
      new PublicKey("166bf3765ebd1fc55decfe395beff2ea3b2a4e0a8946e7eb578512b555737c99");

  private static ChatMessage.Builder aMessageFromAlice() {
    return ChatMessage.builder().from(ALICE).to(BOB).content("Hola, que tal?");
  }

  /** A chat message becomes the kind-14 rumor that NIP-17 specifies. */
  @Test
  @DisplayName("renders as a kind-14 rumor")
  void rendersAsChatMessageRumor() {
    Rumor rumor = aMessageFromAlice().build().toRumor();

    assertEquals(Kinds.CHAT_MESSAGE, rumor.getKind());
    assertEquals(ALICE, rumor.getPubKey());
    assertEquals("Hola, que tal?", rumor.getContent());
    assertTrue(rumor.hasValidId());
  }

  /** Recipients are carried as p tags, which is how NIP-17 identifies a conversation. */
  @Test
  @DisplayName("carries recipients as p tags")
  void carriesRecipientsAsPTags() {
    Rumor rumor = aMessageFromAlice().to(CAROL).build().toRumor();

    assertEquals(List.of(BOB, CAROL), rumor.getReferencedPublicKeys());
  }

  /** A message survives the round trip through a rumor unchanged. */
  @Test
  @DisplayName("round-trips through a rumor")
  void roundTripsThroughRumor() {
    ChatMessage original =
        aMessageFromAlice().subject("Dinner").inReplyTo("a".repeat(64)).at(1691518405L).build();

    ChatMessage restored = ChatMessage.from(original.toRumor());

    assertEquals(original, restored);
  }

  /** The subject titles the conversation and survives the round trip. */
  @Test
  @DisplayName("carries a subject")
  void carriesSubject() {
    ChatMessage restored =
        ChatMessage.from(aMessageFromAlice().subject("Dinner").build().toRumor());

    assertEquals("Dinner", restored.getSubject().orElseThrow());
  }

  /** A reply names its parent through an e tag. */
  @Test
  @DisplayName("carries a reply reference as an e tag")
  void carriesReplyReference() {
    String parentId = "b".repeat(64);

    ChatMessage restored =
        ChatMessage.from(aMessageFromAlice().inReplyTo(parentId).build().toRumor());

    assertEquals(parentId, restored.getReplyTo().orElseThrow());
  }

  /**
   * Participants include the sender as well as the recipients, because NIP-17 requires a copy
   * addressed to the sender so they retain their own history.
   */
  @Test
  @DisplayName("counts the sender among the participants")
  void countsSenderAmongParticipants() {
    ChatMessage message = aMessageFromAlice().to(CAROL).build();

    assertEquals(List.of(BOB, CAROL, ALICE), message.getParticipants());
  }

  /** A sender messaging only themselves appears once, not twice. */
  @Test
  @DisplayName("lists a self-addressed sender only once")
  void listsSelfAddressedSenderOnce() {
    ChatMessage note = ChatMessage.builder().from(ALICE).to(ALICE).content("note to self").build();

    assertEquals(List.of(ALICE), note.getParticipants());
  }

  /** A recipient named twice is carried once, so a conversation is not accidentally redefined. */
  @Test
  @DisplayName("ignores a duplicate recipient")
  void ignoresDuplicateRecipient() {
    ChatMessage message = aMessageFromAlice().to(BOB).build();

    assertEquals(List.of(BOB), message.getRecipients());
  }

  /** A message without a sender cannot be built, since it could not be sealed. */
  @Test
  @DisplayName("refuses to build without a sender")
  void refusesToBuildWithoutSender() {
    ChatMessage.Builder builder = ChatMessage.builder().to(BOB).content("hello");

    assertThrows(IllegalStateException.class, builder::build);
  }

  /** A message without a recipient cannot be built, since it would reach nobody. */
  @Test
  @DisplayName("refuses to build without a recipient")
  void refusesToBuildWithoutRecipient() {
    ChatMessage.Builder builder = ChatMessage.builder().from(ALICE).content("hello");

    assertThrows(IllegalStateException.class, builder::build);
  }

  /** A rumor of another kind is not a chat message and is refused. */
  @Test
  @DisplayName("refuses to read a rumor that is not a chat message")
  void refusesNonChatMessageRumor() {
    Rumor note = Rumor.create(ALICE, Kinds.TEXT_NOTE, List.of(BaseTag.create("p", BOB.toString())), "hi");

    assertThrows(IllegalArgumentException.class, () -> ChatMessage.from(note));
  }

  /** A message stamps itself with the current time when none is given. */
  @Test
  @DisplayName("defaults the timestamp to now")
  void defaultsTimestampToNow() {
    long before = Instant.now().getEpochSecond();

    ChatMessage message = aMessageFromAlice().build();

    assertTrue(message.getCreatedAt() >= before);
  }

  /** The string form must not disclose message content. */
  @Test
  @DisplayName("omits content from toString")
  void omitsContentFromToString() {
    ChatMessage message =
        ChatMessage.builder().from(ALICE).to(BOB).content("meet me at the usual place").build();

    assertFalse(message.toString().contains("usual place"));
  }
}
