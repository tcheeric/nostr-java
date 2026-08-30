package nostr.mcp.write;

import java.util.Locale;

/**
 * How much freedom an agent has to publish.
 *
 * <p>Publishing to Nostr is public and irreversible: NIP-09 deletion is advisory, so a relay may
 * keep an event forever whatever its author later asks. A hallucinated post is therefore not a
 * recoverable mistake, and the deployment decides in advance how much trust the agent gets.
 */
public enum WritePolicy {

  /** Write tools are not registered, so a read-only server cannot be talked into posting. */
  DENY,

  /** Write tools preview first and publish only when the agent returns the token. */
  CONFIRM,

  /** Writes proceed directly, for automation whose output is already trusted. */
  ALLOW;

  /**
   * Read the configured policy, defaulting to the cautious one.
   *
   * <p>An unrecognised value falls back to {@link #CONFIRM} rather than failing: a typo in a
   * config file should not stop a server from starting, but neither should it quietly grant more
   * freedom than the operator asked for.
   *
   * @param configured the value from configuration, which may be null
   * @return the policy to enforce
   */
  public static WritePolicy fromConfiguredValue(String configured) {
    if (configured == null || configured.isBlank()) {
      return CONFIRM;
    }
    try {
      return valueOf(configured.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException unrecognised) {
      return CONFIRM;
    }
  }

  /**
   * Whether write tools appear on the surface at all.
   *
   * @return true unless writing is denied
   */
  public boolean allowsWriteTools() {
    return this != DENY;
  }

  /**
   * Whether a write needs a second call carrying a token.
   *
   * @return true when confirmation is required
   */
  public boolean requiresConfirmation() {
    return this == CONFIRM;
  }
}
