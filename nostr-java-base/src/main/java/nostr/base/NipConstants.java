package nostr.base;

/**
 * Shared constants derived from NIP specifications.
 */
public final class NipConstants {

  private NipConstants() {}

  public static final int EVENT_ID_HEX_LENGTH = 64;
  public static final int PUBLIC_KEY_HEX_LENGTH = 64;
  public static final int SIGNATURE_HEX_LENGTH = 128;

  public static final int REPLACEABLE_KIND_MIN = 10_000;
  public static final int REPLACEABLE_KIND_MAX = 20_000;
  public static final int EPHEMERAL_KIND_MIN = 20_000;
  public static final int EPHEMERAL_KIND_MAX = 30_000;
  public static final int ADDRESSABLE_KIND_MIN = 30_000;
  public static final int ADDRESSABLE_KIND_MAX = 40_000;
}
