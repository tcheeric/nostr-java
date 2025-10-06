package nostr.event.support;

import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import nostr.base.NipConstants;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.util.validator.HexStringValidator;

/**
 * Performs NIP-01 validation on {@link GenericEvent} instances.
 */
public final class GenericEventValidator {

  private GenericEventValidator() {}

  public static void validate(@NonNull GenericEvent event) {
    requireHex(event.getId(), NipConstants.EVENT_ID_HEX_LENGTH, "Missing required `id` field.");
    requireHex(
        event.getPubKey() != null ? event.getPubKey().toString() : null,
        NipConstants.PUBLIC_KEY_HEX_LENGTH,
        "Missing required `pubkey` field.");
    requireHex(
        event.getSignature() != null ? event.getSignature().toString() : null,
        NipConstants.SIGNATURE_HEX_LENGTH,
        "Missing required `sig` field.");

    if (event.getCreatedAt() == null || event.getCreatedAt() < 0) {
      throw new AssertionError("Invalid `created_at`: Must be a non-negative integer.");
    }

    validateKind(event.getKind());
    validateTags(event.getTags());
    validateContent(event.getContent());
  }

  private static void requireHex(String value, int length, String missingMessage) {
    Objects.requireNonNull(value, missingMessage);
    HexStringValidator.validateHex(value, length);
  }

  public static void validateKind(Integer kind) {
    if (kind == null || kind < 0) {
      throw new AssertionError("Invalid `kind`: Must be a non-negative integer.");
    }
  }

  public static void validateTags(List<BaseTag> tags) {
    if (tags == null) {
      throw new AssertionError("Invalid `tags`: Must be a non-null array.");
    }
  }

  public static void validateContent(String content) {
    if (content == null) {
      throw new AssertionError("Invalid `content`: Must be a string.");
    }
  }
}
