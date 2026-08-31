package nostr.mcp.identity;

import java.util.Collection;
import java.util.List;

/**
 * Thrown when a caller names an identity the vault does not hold.
 *
 * <p>Lists the aliases that do exist, because an agent that named the wrong one can correct
 * itself from that and cannot from "unknown identity". The public keys are not listed: knowing
 * which aliases exist is navigation, and knowing whose keys they are is disclosure.
 */
public class IdentityUnknownException extends RuntimeException {

  private final transient Collection<String> knownAliases;

  /**
   * @param alias the alias that was asked for
   * @param knownAliases the aliases the vault does hold
   */
  public IdentityUnknownException(String alias, Collection<String> knownAliases) {
    super(
        knownAliases.isEmpty()
            ? "No identity named '" + alias + "'; the keystore is empty"
            : "No identity named '" + alias + "'; known aliases are " + knownAliases);
    this.knownAliases = List.copyOf(knownAliases);
  }

  /**
   * The aliases the vault holds.
   *
   * @return the known aliases
   */
  public Collection<String> getKnownAliases() {
    return knownAliases;
  }
}
