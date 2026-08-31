package nostr.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.Rumor;
import nostr.event.json.EventJsonMapper;
import nostr.id.Identity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies NIP-59 gift wrapping, including the checks that authenticate an incoming message.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 */
class Nip59GiftWrapperTest {

  /** Keys from the worked example published in NIP-59, section "An Example". */
  private static final String AUTHOR_PRIVATE_KEY =
      "0beebd062ec8735f4243466049d7747ef5d6594ee838de147f8aab842b15e273";

  private static final String RECIPIENT_PRIVATE_KEY =
      "e108399bd8424357a710b606ae0c13166d853d327e47a6e5e038197346bdbf45";

  private static final String EPHEMERAL_PRIVATE_KEY =
      "4f02eac59266002db5801adc5270700ca69d5b8f761d8732fab2fbf233c90cbd";

  private static final long SEAL_CREATED_AT = 1703015180L;
  private static final long WRAP_CREATED_AT = 1703021488L;
  private static final long RUMOR_CREATED_AT = 1691518405L;

  private static final Identity AUTHOR = Identity.create(AUTHOR_PRIVATE_KEY);
  private static final Identity RECIPIENT = Identity.create(RECIPIENT_PRIVATE_KEY);

  /** A wrapper whose ephemeral key and timestamps are fixed, so output is reproducible. */
  private static Nip59GiftWrapper deterministicWrapper(Identity identity) {
    return new Nip59GiftWrapper(
        identity,
        Kinds.GIFT_WRAP,
        () -> Identity.create(EPHEMERAL_PRIVATE_KEY),
        reference -> reference == RUMOR_CREATED_AT ? SEAL_CREATED_AT : WRAP_CREATED_AT);
  }

  private static Rumor nip59ExampleRumor() {
    return new Rumor(
        null,
        AUTHOR.getPublicKey(),
        RUMOR_CREATED_AT,
        Kinds.TEXT_NOTE,
        List.of(),
        "Are you going to the party tonight?");
  }

  private static Rumor aChatMessage(Identity from, PublicKey to, String content) {
    return Rumor.create(
        from.getPublicKey(), Kinds.CHAT_MESSAGE, List.of(BaseTag.create("p", to.toString())), content);
  }

  /**
   * The gift wrap produced for the NIP-59 worked example matches the published event: same
   * ephemeral author, same kind, same recipient tag, and same timestamp. This pins the
   * implementation to the specification rather than to our reading of it.
   */
  @Test
  @DisplayName("reproduces the gift wrap published in the NIP-59 example")
  void reproducesPublishedGiftWrap() {
    GenericEvent wrap =
        deterministicWrapper(AUTHOR).wrap(nip59ExampleRumor(), RECIPIENT.getPublicKey());

    assertEquals(
        "18b1a75918f1f2c90c23da616bce317d36e348bcf5f7ba55e75949319210c87c",
        wrap.getPubKey().toString(),
        "wrap must be signed by the ephemeral key, not the author");
    assertEquals(Kinds.GIFT_WRAP, wrap.getKind());
    assertEquals(WRAP_CREATED_AT, wrap.getCreatedAt());
    assertEquals(1, wrap.getTags().size());
  }

  /**
   * The recipient of the published example can open our wrap and recover the exact rumor, which
   * confirms our ciphertext is readable by an independent reading of the spec.
   */
  @Test
  @DisplayName("produces a wrap the NIP-59 example recipient can open")
  void producesWrapTheExampleRecipientCanOpen() {
    GenericEvent wrap =
        deterministicWrapper(AUTHOR).wrap(nip59ExampleRumor(), RECIPIENT.getPublicKey());

    Rumor opened = new Nip59GiftWrapper(RECIPIENT).unwrap(wrap);

    assertEquals("Are you going to the party tonight?", opened.getContent());
    assertEquals(AUTHOR.getPublicKey(), opened.getPubKey());
    assertEquals(
        "9dd003c6d3b73b74a85a9ab099469ce251653a7af76f523671ab828acd2a0ef9", opened.getId());
  }

  /** A rumor survives a wrap and unwrap unchanged. */
  @Test
  @DisplayName("round-trips a rumor through wrap and unwrap")
  void roundTripsRumor() {
    Rumor original = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "Hola, que tal?");

    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(original, RECIPIENT.getPublicKey());
    Rumor opened = new Nip59GiftWrapper(RECIPIENT).unwrap(wrap);

    assertEquals(original, opened);
  }

  /** Content that stresses JSON escaping survives the round trip intact. */
  @Test
  @DisplayName("round-trips content that needs JSON escaping")
  void roundTripsAwkwardContent() {
    String awkward = "quote\" backslash\\ newline\n control\u0001 astral\uD83D\uDE80";
    Rumor original = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), awkward);

    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(original, RECIPIENT.getPublicKey());
    Rumor opened = new Nip59GiftWrapper(RECIPIENT).unwrap(wrap);

    assertEquals(awkward, opened.getContent());
  }

  /**
   * A long message survives the round trip. The NIP-44 payload caps at 65,535 bytes and each
   * layer wraps the one below in JSON, so the usable message is meaningfully smaller than the
   * cap; this exercises a message large enough to cross several NIP-44 padding buckets.
   */
  @Test
  @DisplayName("round-trips a long message")
  void roundTripsLargeMessage() {
    Rumor original = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "x".repeat(20_000));

    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(original, RECIPIENT.getPublicKey());
    Rumor opened = new Nip59GiftWrapper(RECIPIENT).unwrap(wrap);

    assertEquals(original.getContent(), opened.getContent());
  }

  /**
   * A forged message, where the rumor names an author other than the key that sealed it, is
   * rejected. Without this check anyone could attribute a message to anyone, because the rumor
   * itself is unsigned. NIP-17 requires the comparison explicitly.
   */
  @Test
  @DisplayName("rejects a rumor whose author differs from the sealing key")
  void rejectsImpersonatedRumor() {
    Identity attacker = Identity.generateRandomIdentity();
    Rumor forged =
        Rumor.create(
            AUTHOR.getPublicKey(),
            Kinds.CHAT_MESSAGE,
            List.of(),
            "Transfer the funds, this is definitely me");

    GenericEvent wrap = new Nip59GiftWrapper(attacker).wrap(forged, RECIPIENT.getPublicKey());

    GiftWrapException rejection =
        assertThrows(GiftWrapException.class, () -> new Nip59GiftWrapper(RECIPIENT).unwrap(wrap));
    assertTrue(rejection.getMessage().contains("forged"));
  }

  /**
   * A seal whose contents were altered after signing is rejected, because its signature no
   * longer matches. Accepting it would mean accepting an unauthenticated message.
   */
  @Test
  @DisplayName("rejects a seal whose signature does not verify")
  void rejectsSealWithInvalidSignature() {
    Identity ephemeral = Identity.create(EPHEMERAL_PRIVATE_KEY);

    GenericEvent forgedSeal = new GenericEvent(AUTHOR.getPublicKey(), Kinds.SEAL);
    forgedSeal.setContent("not-the-content-that-was-signed");
    forgedSeal.setTags(List.of());
    forgedSeal.update(SEAL_CREATED_AT);
    // Signed by a different key than the seal claims, so verification must fail.
    ephemeral.sign(forgedSeal);

    GenericEvent wrap = wrapRaw(forgedSeal, ephemeral, RECIPIENT.getPublicKey());

    GiftWrapException rejection =
        assertThrows(GiftWrapException.class, () -> new Nip59GiftWrapper(RECIPIENT).unwrap(wrap));
    assertTrue(rejection.getMessage().contains("signature"));
  }

  /** A seal carrying no signature at all is rejected. */
  @Test
  @DisplayName("rejects a seal carrying no signature")
  void rejectsUnsignedSeal() {
    Identity ephemeral = Identity.generateRandomIdentity();

    GenericEvent unsignedSeal = new GenericEvent(AUTHOR.getPublicKey(), Kinds.SEAL);
    unsignedSeal.setContent("anything");
    unsignedSeal.setTags(List.of());
    unsignedSeal.update(SEAL_CREATED_AT);

    GenericEvent wrap = wrapRaw(unsignedSeal, ephemeral, RECIPIENT.getPublicKey());

    assertThrows(GiftWrapException.class, () -> new Nip59GiftWrapper(RECIPIENT).unwrap(wrap));
  }

  /** An inner event that is not a kind-13 seal is rejected. */
  @Test
  @DisplayName("rejects a wrap that does not contain a seal")
  void rejectsWrapWithoutSeal() {
    Identity ephemeral = Identity.generateRandomIdentity();

    GenericEvent notASeal = new GenericEvent(AUTHOR.getPublicKey(), Kinds.TEXT_NOTE);
    notASeal.setContent("a plain note, not a seal");
    notASeal.setTags(List.of());
    notASeal.update(SEAL_CREATED_AT);
    AUTHOR.sign(notASeal);

    GenericEvent wrap = wrapRaw(notASeal, ephemeral, RECIPIENT.getPublicKey());

    assertThrows(GiftWrapException.class, () -> new Nip59GiftWrapper(RECIPIENT).unwrap(wrap));
  }

  /** A wrap addressed to somebody else cannot be opened, and says so rather than failing oddly. */
  @Test
  @DisplayName("rejects a wrap addressed to a different recipient")
  void rejectsWrapForAnotherRecipient() {
    Identity stranger = Identity.generateRandomIdentity();
    Rumor rumor = aChatMessage(AUTHOR, stranger.getPublicKey(), "not for you");

    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(rumor, stranger.getPublicKey());

    assertThrows(GiftWrapException.class, () -> new Nip59GiftWrapper(RECIPIENT).unwrap(wrap));
  }

  /** A wrap whose ciphertext was corrupted in transit is rejected. */
  @Test
  @DisplayName("rejects a wrap with corrupted ciphertext")
  void rejectsCorruptedCiphertext() {
    Rumor rumor = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "hello");
    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(rumor, RECIPIENT.getPublicKey());

    GenericEvent corrupted = new GenericEvent(wrap.getPubKey(), Kinds.GIFT_WRAP);
    corrupted.setContent(wrap.getContent().substring(0, wrap.getContent().length() - 8) + "AAAAAAAA");
    corrupted.setTags(wrap.getTags());
    corrupted.update(wrap.getCreatedAt());

    assertThrows(GiftWrapException.class, () -> new Nip59GiftWrapper(RECIPIENT).unwrap(corrupted));
  }

  /**
   * Each wrap is signed by a different key, so an observer cannot link two messages from the
   * same author. Reusing an ephemeral key would undo the whole scheme.
   */
  @Test
  @DisplayName("signs every wrap with a distinct single-use key")
  void usesDistinctEphemeralKeyPerWrap() {
    Rumor rumor = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "hello");
    Nip59GiftWrapper wrapper = new Nip59GiftWrapper(AUTHOR);

    GenericEvent first = wrapper.wrap(rumor, RECIPIENT.getPublicKey());
    GenericEvent second = wrapper.wrap(rumor, RECIPIENT.getPublicKey());

    assertNotEquals(first.getPubKey(), second.getPubKey());
    assertNotEquals(first.getContent(), second.getContent());
    assertNotEquals(first.getId(), second.getId());
  }

  /** The wrap never carries the author's key, which is the point of the outer layer. */
  @Test
  @DisplayName("never exposes the author's key on the wrap")
  void hidesAuthorOnTheWrap() {
    Rumor rumor = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "hello");

    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(rumor, RECIPIENT.getPublicKey());

    assertNotEquals(AUTHOR.getPublicKey(), wrap.getPubKey());
    assertFalse(wrap.getContent().contains(AUTHOR.getPublicKey().toString()));
  }

  /**
   * Timestamps are randomised into the past and never into the future, since NIP-59 requires it
   * and relays commonly drop future-dated events.
   */
  @Test
  @DisplayName("randomises timestamps into the past, never the future")
  void randomisesTimestampsIntoThePast() {
    Rumor rumor = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "hello");
    Nip59GiftWrapper wrapper = new Nip59GiftWrapper(AUTHOR);
    long twoDays = 2 * 24 * 60 * 60L;

    boolean anyDiffer = false;
    long previous = -1;
    for (int attempt = 0; attempt < 20; attempt++) {
      long now = Instant.now().getEpochSecond();
      GenericEvent wrap = wrapper.wrap(rumor, RECIPIENT.getPublicKey());

      assertTrue(wrap.getCreatedAt() <= now, "wrap must never be dated in the future");
      assertTrue(wrap.getCreatedAt() >= now - twoDays, "wrap must stay within the two-day window");

      anyDiffer |= previous != -1 && previous != wrap.getCreatedAt();
      previous = wrap.getCreatedAt();
    }
    assertTrue(anyDiffer, "timestamps must vary between wraps");
  }

  /** The true send time of the rumor is not disclosed by the wrap. */
  @Test
  @DisplayName("does not reuse the rumor timestamp on the wrap")
  void doesNotLeakRumorTimestamp() {
    Rumor rumor = nip59ExampleRumor();

    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(rumor, RECIPIENT.getPublicKey());

    assertNotEquals(rumor.getCreatedAt(), wrap.getCreatedAt());
  }

  /** Ephemeral wraps carry kind 21059, so relays know not to store them. */
  @Test
  @DisplayName("stamps ephemeral wraps with kind 21059")
  void stampsEphemeralWrapKind() {
    Rumor rumor = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "hello");

    GenericEvent wrap =
        new Nip59GiftWrapper(AUTHOR, Kinds.EPHEMERAL_GIFT_WRAP)
            .wrap(rumor, RECIPIENT.getPublicKey());

    assertEquals(Kinds.EPHEMERAL_GIFT_WRAP, wrap.getKind());
    assertEquals(rumor, new Nip59GiftWrapper(RECIPIENT).unwrap(wrap));
  }

  /** The recipient is named on the wrap so relays can route it, and nothing else is. */
  @Test
  @DisplayName("tags only the recipient on the wrap")
  void tagsOnlyTheRecipient() {
    Rumor rumor = aChatMessage(AUTHOR, RECIPIENT.getPublicKey(), "hello");

    GenericEvent wrap = new Nip59GiftWrapper(AUTHOR).wrap(rumor, RECIPIENT.getPublicKey());

    assertEquals(1, wrap.getTags().size());
    assertEquals("p", wrap.getTags().get(0).getCode());
  }

  /** Round trips hold across many random identities and messages. */
  @Test
  @DisplayName("round-trips across many random identities")
  void roundTripsAcrossManyIdentities() {
    for (int attempt = 0; attempt < 15; attempt++) {
      Identity sender = Identity.generateRandomIdentity();
      Identity receiver = Identity.generateRandomIdentity();
      Rumor rumor = aChatMessage(sender, receiver.getPublicKey(), "message " + attempt);

      GenericEvent wrap = new Nip59GiftWrapper(sender).wrap(rumor, receiver.getPublicKey());

      assertEquals(rumor, new Nip59GiftWrapper(receiver).unwrap(wrap));
    }
  }

  /**
   * Builds a gift wrap around an arbitrary inner event, the way an attacker would, bypassing
   * the seal construction that {@link Nip59GiftWrapper#wrap} performs. This is what lets the
   * tests present malformed and forged seals to the receive path.
   */
  private static GenericEvent wrapRaw(
      GenericEvent innerEvent, Identity ephemeral, PublicKey recipient) {
    String innerJson;
    try {
      innerJson = EventJsonMapper.getMapper().writeValueAsString(innerEvent);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize test event", ex);
    }

    String ciphertext =
        new MessageCipher44(ephemeral.getPrivateKey().getRawData(), recipient.getRawData())
            .encrypt(innerJson);

    GenericEvent wrap = new GenericEvent(ephemeral.getPublicKey(), Kinds.GIFT_WRAP);
    wrap.setContent(ciphertext);
    wrap.setTags(List.of(BaseTag.create("p", recipient.toString())));
    wrap.update(WRAP_CREATED_AT);
    ephemeral.sign(wrap);

    return wrap;
  }
}
