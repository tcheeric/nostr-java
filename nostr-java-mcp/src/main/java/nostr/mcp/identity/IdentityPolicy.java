package nostr.mcp.identity;

import nostr.mcp.write.WritePolicy;

import java.util.Locale;

/**
 * How much freedom an agent has to change the keystore.
 *
 * <p>Separate from the write policy because the two risks are different in kind. Publishing is
 * public and irreversible but bounded to one event; destroying a key ends an account and orphans
 * every event ever signed with it. A deployment may reasonably let an agent post while forbidding
 * it to touch the keys, and collapsing the two would force a choice between a useless server and
 * an unsafe one.
 */
public enum IdentityPolicy {

  /** Keystore-mutating tools are not registered. */
  DENY,

  /** Irreversible mutations need a second call carrying a token. */
  CONFIRM,

  /** Mutations proceed directly, for automation that provisions its own identities. */
  ALLOW;

  /**
   * Read the configured policy, never granting more than the write policy does.
   *
   * <p>A read-only server must not be able to destroy a key, so {@code write-policy: deny}
   * implies no mutation whatever this setting says. Defaulting to the more restrictive of the two
   * means the safe combination needs no configuration, which is the one people get right.
   *
   * @param configured the value from configuration, which may be null
   * @param writePolicy the write policy, which caps this one
   * @return the policy to enforce
   */
  public static IdentityPolicy fromConfiguredValue(String configured, WritePolicy writePolicy) {
    if (writePolicy == WritePolicy.DENY) {
      return DENY;
    }
    IdentityPolicy requested = parse(configured);
    return requested.isMorePermissiveThan(writePolicy) ? matching(writePolicy) : requested;
  }

  /**
   * Whether keystore-mutating tools appear on the surface at all.
   *
   * @return true unless mutation is denied
   */
  public boolean allowsMutation() {
    return this != DENY;
  }

  /**
   * Whether an irreversible mutation needs a second call.
   *
   * @return true when confirmation is required
   */
  public boolean requiresConfirmation() {
    return this != ALLOW;
  }

  private static IdentityPolicy parse(String configured) {
    if (configured == null || configured.isBlank()) {
      return CONFIRM;
    }
    try {
      return valueOf(configured.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException unrecognised) {
      return CONFIRM;
    }
  }

  private boolean isMorePermissiveThan(WritePolicy writePolicy) {
    return this == ALLOW && writePolicy == WritePolicy.CONFIRM;
  }

  private static IdentityPolicy matching(WritePolicy writePolicy) {
    return writePolicy == WritePolicy.ALLOW ? ALLOW : CONFIRM;
  }
}
