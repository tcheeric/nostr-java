package nostr.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import nostr.base.Kinds;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.schnorr.Schnorr;
import nostr.crypto.schnorr.SchnorrException;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.Rumor;
import nostr.event.json.EventJsonMapper;
import nostr.event.serializer.EventSerializer;
import nostr.id.Identity;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

/**
 * Gift wraps events according to NIP-59.
 *
 * <p>Each call to {@link #wrap} generates a key that signs exactly one event and is then
 * discarded. Reusing such a key across recipients would let an observer link the wraps back
 * together and identify a conversation, which is the very thing the wrap exists to prevent.
 *
 * <p>Timestamps on the seal and the wrap are drawn independently from the two days preceding
 * now. NIP-59 requires them to be randomised so that events cannot be correlated by time, and
 * requires them to be in the past because relays commonly reject future-dated events.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 */
public class Nip59GiftWrapper implements GiftWrapper {

  private static final long TWO_DAYS_IN_SECONDS = 2 * 24 * 60 * 60L;
  private static final ObjectMapper MAPPER = EventJsonMapper.getMapper();

  private final Identity identity;
  private final int wrapKind;
  private final EphemeralKeySource ephemeralKeySource;
  private final TimestampRandomizer timestampRandomizer;

  /**
   * Creates a wrapper that produces stored gift wraps for the given identity.
   *
   * @param identity the identity that seals rumors and opens wraps addressed to it
   */
  public Nip59GiftWrapper(@NonNull Identity identity) {
    this(identity, Kinds.GIFT_WRAP);
  }

  /**
   * Creates a wrapper that produces gift wraps of a chosen kind.
   *
   * <p>Use {@link Kinds#GIFT_WRAP} for messages that should be stored and delivered later, and
   * {@link Kinds#EPHEMERAL_GIFT_WRAP} for real-time exchanges that relays must not retain.
   *
   * @param identity the identity that seals rumors and opens wraps addressed to it
   * @param wrapKind the kind to stamp on the outer event
   */
  public Nip59GiftWrapper(@NonNull Identity identity, int wrapKind) {
    this(identity, wrapKind, Identity::generateRandomIdentity, new PastTwoDaysRandomizer());
  }

  /**
   * Creates a wrapper with explicit sources of randomness.
   *
   * <p>Intended for tests that reproduce the worked examples published in NIP-17 and NIP-59,
   * which fix the ephemeral key and both timestamps.
   *
   * @param identity the identity that seals rumors and opens wraps addressed to it
   * @param wrapKind the kind to stamp on the outer event
   * @param ephemeralKeySource supplies the single-use key that signs each wrap
   * @param timestampRandomizer supplies the randomised timestamps for the seal and the wrap
   */
  public Nip59GiftWrapper(
      @NonNull Identity identity,
      int wrapKind,
      @NonNull EphemeralKeySource ephemeralKeySource,
      @NonNull TimestampRandomizer timestampRandomizer) {
    this.identity = identity;
    this.wrapKind = wrapKind;
    this.ephemeralKeySource = ephemeralKeySource;
    this.timestampRandomizer = timestampRandomizer;
  }

  @Override
  public GenericEvent wrap(@NonNull Rumor rumor, @NonNull PublicKey recipient) {
    GenericEvent seal = seal(rumor, recipient);
    Identity ephemeral = ephemeralKeySource.generate();

    String encryptedSeal = encrypt(toJson(seal), ephemeral.getPrivateKey(), recipient);

    GenericEvent giftWrap = new GenericEvent(ephemeral.getPublicKey(), wrapKind);
    giftWrap.setContent(encryptedSeal);
    giftWrap.setTags(List.of(BaseTag.create("p", recipient.toString())));
    giftWrap.update(timestampRandomizer.randomizeFrom(Instant.now().getEpochSecond()));
    ephemeral.sign(giftWrap);

    return giftWrap;
  }

  @Override
  public Rumor unwrap(@NonNull GenericEvent giftWrap) {
    GenericEvent seal = openSeal(giftWrap);
    verifySealSignature(seal);

    Rumor rumor = decryptRumor(seal);
    verifyAuthorMatchesSeal(rumor, seal);
    verifyRumorId(rumor);

    return rumor;
  }

  /**
   * Encrypts a rumor to its recipient and signs the result as a kind-13 seal.
   *
   * <p>The seal carries no tags. Anything placed on it is visible to an observer who holds the
   * gift wrap, so tags here would leak the very metadata the wrap conceals.
   */
  private GenericEvent seal(Rumor rumor, PublicKey recipient) {
    String encryptedRumor = encrypt(toJson(rumor), identity.getPrivateKey(), recipient);

    GenericEvent seal = new GenericEvent(identity.getPublicKey(), Kinds.SEAL);
    seal.setContent(encryptedRumor);
    seal.setTags(List.of());
    seal.update(timestampRandomizer.randomizeFrom(rumor.getCreatedAt()));
    identity.sign(seal);

    return seal;
  }

  private GenericEvent openSeal(GenericEvent giftWrap) {
    String sealJson =
        decrypt(giftWrap.getContent(), identity.getPrivateKey(), giftWrap.getPubKey());
    GenericEvent seal = parse(sealJson, GenericEvent.class, "seal");

    if (!Integer.valueOf(Kinds.SEAL).equals(seal.getKind())) {
      throw new GiftWrapException("Gift wrap did not contain a kind-13 seal");
    }
    return seal;
  }

  private Rumor decryptRumor(GenericEvent seal) {
    String rumorJson = decrypt(seal.getContent(), identity.getPrivateKey(), seal.getPubKey());
    return parse(rumorJson, Rumor.class, "rumor");
  }

  /**
   * Confirms the seal was signed by the key it claims.
   *
   * <p>A rumor carries no signature, so the seal's signature is the only evidence of who wrote
   * the message. An unverified seal is an unauthenticated message.
   */
  private void verifySealSignature(GenericEvent seal) {
    Signature signature = seal.getSignature();
    if (signature == null) {
      throw new GiftWrapException("Seal carried no signature");
    }

    try {
      byte[] serialized =
          EventSerializer.serializeToBytes(
              seal.getPubKey(),
              seal.getCreatedAt(),
              seal.getKind(),
              seal.getTags(),
              seal.getContent());
      boolean valid =
          Schnorr.verify(
              NostrUtil.sha256(serialized),
              seal.getPubKey().getRawData(),
              signature.getRawData());
      if (!valid) {
        throw new GiftWrapException("Seal signature did not verify");
      }
    } catch (SchnorrException | NoSuchAlgorithmException | NostrException ex) {
      throw new GiftWrapException("Could not verify seal signature", ex);
    }
  }

  /**
   * Confirms the rumor names the same author as the seal that carried it.
   *
   * <p>Without this check anyone could attribute a message to anyone else by editing the
   * rumor's author before sealing it, because only the seal is signed. NIP-17 requires the
   * comparison for exactly this reason.
   */
  private void verifyAuthorMatchesSeal(Rumor rumor, GenericEvent seal) {
    if (!rumor.getPubKey().equals(seal.getPubKey())) {
      throw new GiftWrapException(
          "Rumor author does not match the sealing key; the message is forged");
    }
  }

  /** Confirms the rumor's contents still hash to the id it carries. */
  private void verifyRumorId(Rumor rumor) {
    if (!rumor.hasValidId()) {
      throw new GiftWrapException("Rumor id does not match its contents");
    }
  }

  private String encrypt(String plaintext, PrivateKey sender, PublicKey recipient) {
    return new MessageCipher44(sender.getRawData(), recipient.getRawData()).encrypt(plaintext);
  }

  private String decrypt(String payload, PrivateKey self, PublicKey other) {
    try {
      return new MessageCipher44(self.getRawData(), other.getRawData()).decrypt(payload);
    } catch (RuntimeException ex) {
      throw new GiftWrapException("Could not decrypt payload; it is not addressed to us", ex);
    }
  }

  private static String toJson(Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      throw new GiftWrapException("Failed to serialize event for wrapping", ex);
    }
  }

  private static <T> T parse(String json, Class<T> type, String description) {
    try {
      return MAPPER.readValue(json, type);
    } catch (JsonProcessingException ex) {
      throw new GiftWrapException("Decrypted payload was not a valid " + description, ex);
    }
  }

  /** Supplies the single-use identity that signs one gift wrap. */
  @FunctionalInterface
  public interface EphemeralKeySource {
    Identity generate();
  }

  /** Chooses the randomised timestamp carried by a seal or a gift wrap. */
  @FunctionalInterface
  public interface TimestampRandomizer {

    /**
     * Returns a timestamp at or before {@code referenceEpochSeconds}, within two days of it.
     *
     * @param referenceEpochSeconds the true time being obscured
     * @return the timestamp to publish
     */
    long randomizeFrom(long referenceEpochSeconds);
  }

  /**
   * Draws a timestamp uniformly from the two days preceding the reference time.
   *
   * <p>The offset comes from {@link SecureRandom}: a predictable offset would be no protection
   * when the offset is what conceals the true send time. Timestamps never move into the future,
   * which NIP-59 requires and which keeps relays from dropping the event.
   */
  public static final class PastTwoDaysRandomizer implements TimestampRandomizer {

    private final SecureRandom random = new SecureRandom();

    @Override
    public long randomizeFrom(long referenceEpochSeconds) {
      return referenceEpochSeconds - random.nextLong(TWO_DAYS_IN_SECONDS + 1);
    }
  }
}
