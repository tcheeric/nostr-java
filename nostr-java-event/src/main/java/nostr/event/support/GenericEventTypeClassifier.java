package nostr.event.support;

/**
 * Utility to classify generic events according to NIP-01 ranges.
 */
public final class GenericEventTypeClassifier {

  private static final int REPLACEABLE_MIN = 10_000;
  private static final int REPLACEABLE_MAX = 20_000;
  private static final int EPHEMERAL_MIN = 20_000;
  private static final int EPHEMERAL_MAX = 30_000;
  private static final int ADDRESSABLE_MIN = 30_000;
  private static final int ADDRESSABLE_MAX = 40_000;

  private GenericEventTypeClassifier() {}

  public static boolean isReplaceable(Integer kind) {
    return kind != null && kind >= REPLACEABLE_MIN && kind < REPLACEABLE_MAX;
  }

  public static boolean isEphemeral(Integer kind) {
    return kind != null && kind >= EPHEMERAL_MIN && kind < EPHEMERAL_MAX;
  }

  public static boolean isAddressable(Integer kind) {
    return kind != null && kind >= ADDRESSABLE_MIN && kind < ADDRESSABLE_MAX;
  }
}
