package nostr.event.validator;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.util.validator.HexStringValidator;

import java.util.List;
import java.util.Objects;

/**
 * Validates Nostr events according to NIP-01 specification.
 *
 * <p>This validator enforces the required fields and format constraints for valid Nostr events:
 * <ul>
 *   <li><b>Event ID:</b> 64-character hex string (32 bytes SHA-256 hash)</li>
 *   <li><b>Public Key:</b> 64-character hex string (32 bytes secp256k1 public key)</li>
 *   <li><b>Signature:</b> 128-character hex string (64 bytes BIP-340 Schnorr signature)</li>
 *   <li><b>Created At:</b> Non-negative Unix timestamp (seconds since epoch)</li>
 *   <li><b>Kind:</b> Non-negative integer event type (see {@link nostr.base.Kind})</li>
 *   <li><b>Tags:</b> Non-null array (can be empty)</li>
 *   <li><b>Content:</b> Non-null string (can be empty)</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * // Validate individual fields
 * try {
 *     EventValidator.validateId(eventId);
 *     EventValidator.validatePubKey(publicKey);
 *     EventValidator.validateSignature(signature);
 *     // Event fields are valid
 * } catch (AssertionError | NullPointerException e) {
 *     // Handle validation error
 *     log.error("Invalid event field: {}", e.getMessage());
 * }
 *
 * // Validate all fields at once
 * EventValidator.validate(id, pubKey, signature, createdAt, kind, tags, content);
 * }</pre>
 *
 * <p><b>Design:</b> This class uses the Utility Pattern with static methods. It is
 * stateless and thread-safe. All methods throw {@link AssertionError} for validation
 * failures and {@link NullPointerException} for null required fields.
 *
 * <p><b>Reusability:</b> This validator can be used:
 * <ul>
 *   <li>By {@link nostr.event.impl.GenericEvent#validate()} for event validation</li>
 *   <li>In subclasses for NIP-specific validation</li>
 *   <li>Standalone for validating events from any source</li>
 * </ul>
 *
 * @see nostr.event.impl.GenericEvent#validate()
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
 * @since 0.6.2
 */
public final class EventValidator {

  private EventValidator() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Validates all required fields of a Nostr event according to NIP-01.
   *
   * @param id event ID (64 hex chars)
   * @param pubKey public key
   * @param signature Schnorr signature
   * @param createdAt Unix timestamp
   * @param kind event kind
   * @param tags event tags
   * @param content event content
   * @throws NullPointerException if any required field is null
   * @throws AssertionError if any field fails validation
   */
  public static void validate(
      String id,
      PublicKey pubKey,
      Signature signature,
      Long createdAt,
      Integer kind,
      List<BaseTag> tags,
      String content) {
    validateId(id);
    validatePubKey(pubKey);
    validateSignature(signature);
    validateCreatedAt(createdAt);
    validateKind(kind);
    validateTags(tags);
    validateContent(content);
  }

  /**
   * Validates event ID field.
   *
   * @param id the event ID to validate
   * @throws NullPointerException if id is null
   * @throws AssertionError if id is not 64 hex characters
   */
  public static void validateId(@NonNull String id) {
    Objects.requireNonNull(id, "Missing required `id` field.");
    HexStringValidator.validateHex(id, 64);
  }

  /**
   * Validates public key field.
   *
   * @param pubKey the public key to validate
   * @throws NullPointerException if pubKey is null
   * @throws AssertionError if pubKey is not 64 hex characters
   */
  public static void validatePubKey(@NonNull PublicKey pubKey) {
    Objects.requireNonNull(pubKey, "Missing required `pubkey` field.");
    HexStringValidator.validateHex(pubKey.toString(), 64);
  }

  /**
   * Validates signature field.
   *
   * @param signature the signature to validate
   * @throws NullPointerException if signature is null
   * @throws AssertionError if signature is not 128 hex characters
   */
  public static void validateSignature(@NonNull Signature signature) {
    Objects.requireNonNull(signature, "Missing required `sig` field.");
    HexStringValidator.validateHex(signature.toString(), 128);
  }

  /**
   * Validates created_at timestamp field.
   *
   * @param createdAt the Unix timestamp to validate
   * @throws AssertionError if createdAt is null or negative
   */
  public static void validateCreatedAt(Long createdAt) {
    if (createdAt == null || createdAt < 0) {
      throw new AssertionError("Invalid `created_at`: Must be a non-negative integer.");
    }
  }

  /**
   * Validates event kind field.
   *
   * @param kind the event kind to validate
   * @throws AssertionError if kind is null or negative
   */
  public static void validateKind(Integer kind) {
    if (kind == null || kind < 0) {
      throw new AssertionError("Invalid `kind`: Must be a non-negative integer.");
    }
  }

  /**
   * Validates tags array field.
   *
   * @param tags the tags array to validate
   * @throws AssertionError if tags is null
   */
  public static void validateTags(List<BaseTag> tags) {
    if (tags == null) {
      throw new AssertionError("Invalid `tags`: Must be a non-null array.");
    }
  }

  /**
   * Validates content field.
   *
   * @param content the content string to validate
   * @throws AssertionError if content is null
   */
  public static void validateContent(String content) {
    if (content == null) {
      throw new AssertionError("Invalid `content`: Must be a string.");
    }
  }
}
