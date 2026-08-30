package nostr.event.unit;

import nostr.base.ISignable;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.Rumor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the unsigned rumor type introduced for NIP-59 gift wrapping.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 */
class RumorTest {

  /** The worked example published in NIP-59, section "An Example". */
  private static final PublicKey NIP59_AUTHOR =
      new PublicKey("611df01bfcf85c26ae65453b772d8f1dfd25c264621c0277e1fc1518686faef9");

  private static final long NIP59_CREATED_AT = 1691518405L;
  private static final String NIP59_CONTENT = "Are you going to the party tonight?";
  private static final String NIP59_EXPECTED_ID =
      "9dd003c6d3b73b74a85a9ab099469ce251653a7af76f523671ab828acd2a0ef9";

  /**
   * The id derived for the rumor published in NIP-59 matches the id the spec states, which pins
   * canonical serialization to the specification rather than to our reading of it.
   */
  @Test
  @DisplayName("derives the exact event id published in the NIP-59 example")
  void derivesIdFromNip59PublishedVector() {
    Rumor rumor =
        new Rumor(null, NIP59_AUTHOR, NIP59_CREATED_AT, Kinds.TEXT_NOTE, List.of(), NIP59_CONTENT);

    assertEquals(NIP59_EXPECTED_ID, rumor.getId());
  }

  /** A rumor reconstructed with the id the spec publishes reports that id as valid. */
  @Test
  @DisplayName("accepts a rumor whose carried id matches its contents")
  void acceptsMatchingId() {
    Rumor rumor =
        new Rumor(
            NIP59_EXPECTED_ID,
            NIP59_AUTHOR,
            NIP59_CREATED_AT,
            Kinds.TEXT_NOTE,
            List.of(),
            NIP59_CONTENT);

    assertTrue(rumor.hasValidId());
  }

  /**
   * A rumor whose content was altered after its id was set is detected, which is what stops a
   * tampered rumor being accepted on the receive path.
   */
  @Test
  @DisplayName("rejects a rumor whose carried id does not match its contents")
  void rejectsTamperedId() {
    Rumor tampered =
        new Rumor(
            NIP59_EXPECTED_ID,
            NIP59_AUTHOR,
            NIP59_CREATED_AT,
            Kinds.TEXT_NOTE,
            List.of(),
            "Are you going to the party tomorrow?");

    assertFalse(tampered.hasValidId());
  }

  /**
   * Content containing characters that a naive JSON escaper mishandles still produces an id that
   * validates, since the id is a hash over the escaped form.
   */
  @Test
  @DisplayName("derives a stable id for content needing JSON escaping")
  void derivesStableIdForContentNeedingEscaping() {
    String awkward = "quote\" backslash\\ newline\n tab\t control\u0001 astral\uD83D\uDE80";

    Rumor rumor = Rumor.create(NIP59_AUTHOR, Kinds.CHAT_MESSAGE, List.of(), awkward);
    Rumor reconstructed =
        new Rumor(
            rumor.getId(),
            NIP59_AUTHOR,
            rumor.getCreatedAt(),
            Kinds.CHAT_MESSAGE,
            List.of(),
            awkward);

    assertTrue(reconstructed.hasValidId());
    assertEquals(rumor.getId(), reconstructed.getId());
  }

  /** Tags participate in the id, so two rumors differing only by tags get different ids. */
  @Test
  @DisplayName("includes tags in the derived id")
  void includesTagsInDerivedId() {
    List<BaseTag> withRecipient =
        List.of(BaseTag.create("p", NIP59_AUTHOR.toString()));

    Rumor untagged =
        new Rumor(null, NIP59_AUTHOR, NIP59_CREATED_AT, Kinds.CHAT_MESSAGE, List.of(), "hello");
    Rumor tagged =
        new Rumor(
            null, NIP59_AUTHOR, NIP59_CREATED_AT, Kinds.CHAT_MESSAGE, withRecipient, "hello");

    assertNotEquals(untagged.getId(), tagged.getId());
  }

  /** The recipients of a chat message are read back from its {@code p} tags, in order. */
  @Test
  @DisplayName("reads recipients from p tags in order")
  void readsRecipientsFromPTags() {
    PublicKey first =
        new PublicKey("918e2da906df4ccd12c8ac672d8335add131a4cf9d27ce42b3bb3625755f0788");
    PublicKey second =
        new PublicKey("166bf3765ebd1fc55decfe395beff2ea3b2a4e0a8946e7eb578512b555737c99");

    Rumor rumor =
        Rumor.create(
            NIP59_AUTHOR,
            Kinds.CHAT_MESSAGE,
            List.of(
                BaseTag.create("p", first.toString()),
                BaseTag.create("subject", "Dinner"),
                BaseTag.create("p", second.toString())),
            "hello");

    assertEquals(List.of(first, second), rumor.getReferencedPublicKeys());
  }

  /** A rumor without p tags reports no recipients rather than failing. */
  @Test
  @DisplayName("reports no recipients when the rumor has no p tags")
  void reportsNoRecipientsWithoutPTags() {
    Rumor rumor = Rumor.create(NIP59_AUTHOR, Kinds.CHAT_MESSAGE, List.of(), "hello");

    assertTrue(rumor.getReferencedPublicKeys().isEmpty());
  }

  /**
   * The tag list handed to a rumor is copied, so a caller mutating their list afterwards cannot
   * change the rumor's contents behind its already-computed id.
   */
  @Test
  @DisplayName("copies the supplied tags so later caller mutation cannot invalidate the id")
  void copiesSuppliedTags() {
    List<BaseTag> mutable = new ArrayList<>();
    mutable.add(BaseTag.create("p", NIP59_AUTHOR.toString()));

    Rumor rumor = Rumor.create(NIP59_AUTHOR, Kinds.CHAT_MESSAGE, mutable, "hello");
    mutable.clear();

    assertEquals(1, rumor.getTags().size());
    assertTrue(rumor.hasValidId());
  }

  /** The tag list a rumor hands out cannot be modified, keeping the instance immutable. */
  @Test
  @DisplayName("exposes tags as an unmodifiable list")
  void exposesUnmodifiableTags() {
    Rumor rumor =
        Rumor.create(
            NIP59_AUTHOR, Kinds.CHAT_MESSAGE, List.of(BaseTag.create("p", "x")), "hello");

    assertThrows(
        UnsupportedOperationException.class, () -> rumor.getTags().add(BaseTag.create("e", "y")));
  }

  /**
   * A rumor exposes no signature accessor. NIP-59 depends on rumors being unsignable, so this
   * asserts the absence is a property of the type rather than a convention.
   */
  @Test
  @DisplayName("has no signature member and is not signable")
  void hasNoSignatureMember() {
    boolean declaresSignature =
        Arrays.stream(Rumor.class.getDeclaredFields())
            .anyMatch(field -> field.getType().getSimpleName().contains("Signature"));
    boolean exposesSignatureAccessor =
        Arrays.stream(Rumor.class.getMethods())
            .anyMatch(method -> method.getName().toLowerCase().contains("sign"));

    assertFalse(declaresSignature, "Rumor must not hold a signature");
    assertFalse(exposesSignatureAccessor, "Rumor must not expose a signing or signature method");
    assertFalse(
        ISignable.class.isAssignableFrom(Rumor.class),
        "Rumor must not be signable");
  }

  /** Two rumors built from identical inputs are equal, so they can be compared after a round trip. */
  @Test
  @DisplayName("treats rumors with identical contents as equal")
  void treatsIdenticalRumorsAsEqual() {
    Rumor first =
        new Rumor(null, NIP59_AUTHOR, NIP59_CREATED_AT, Kinds.CHAT_MESSAGE, List.of(), "hello");
    Rumor second =
        new Rumor(null, NIP59_AUTHOR, NIP59_CREATED_AT, Kinds.CHAT_MESSAGE, List.of(), "hello");

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  /** A rumor's string form must not disclose its content, which travels encrypted. */
  @Test
  @DisplayName("omits content from toString")
  void omitsContentFromToString() {
    Rumor rumor =
        Rumor.create(NIP59_AUTHOR, Kinds.CHAT_MESSAGE, List.of(), "meet me at the usual place");

    assertFalse(rumor.toString().contains("usual place"));
  }
}
