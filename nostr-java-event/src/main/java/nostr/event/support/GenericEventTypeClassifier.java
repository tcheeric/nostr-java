package nostr.event.support;

import nostr.base.NipConstants;

/**
 * Utility to classify generic events according to NIP-01 ranges.
 */
public final class GenericEventTypeClassifier {

  private GenericEventTypeClassifier() {}

  public static boolean isReplaceable(Integer kind) {
    return kind != null
        && kind >= NipConstants.REPLACEABLE_KIND_MIN
        && kind < NipConstants.REPLACEABLE_KIND_MAX;
  }

  public static boolean isEphemeral(Integer kind) {
    return kind != null
        && kind >= NipConstants.EPHEMERAL_KIND_MIN
        && kind < NipConstants.EPHEMERAL_KIND_MAX;
  }

  public static boolean isAddressable(Integer kind) {
    return kind != null
        && kind >= NipConstants.ADDRESSABLE_KIND_MIN
        && kind < NipConstants.ADDRESSABLE_KIND_MAX;
  }
}
