package nostr.mcp.identity;

import lombok.NonNull;

import java.util.Optional;
import java.util.Set;

/**
 * Which identity a server process operates.
 *
 * <p>A process is either <em>bound</em> to exactly one alias or <em>unbound</em> and able to sign
 * as any identity its keystore holds. The distinction is not a preference, it is the module's
 * strongest isolation guarantee: a bound process never decrypts the other entries, so another
 * identity's key is absent from its heap rather than merely out of policy.
 *
 * <p>Binding also removes a class of mistake instead of guarding against it. With one identity
 * the {@code identity} argument disappears from every signing tool, so an agent cannot name the
 * wrong account, because there is no name to give.
 */
public final class IdentityBinding {

  private static final IdentityBinding UNBOUND = new IdentityBinding(null);

  private final String alias;

  private IdentityBinding(String alias) {
    this.alias = alias;
  }

  /**
   * A process that may sign as any identity in its keystore.
   *
   * @return the unbound binding
   */
  public static IdentityBinding unbound() {
    return UNBOUND;
  }

  /**
   * A process bound to one identity.
   *
   * @param alias the only identity this process may operate
   * @return the binding
   */
  public static IdentityBinding to(@NonNull String alias) {
    return new IdentityBinding(alias);
  }

  /**
   * Read a binding from configuration, where absent means unbound.
   *
   * @param alias the configured alias, or {@code null} or blank when none is set
   * @return the binding the configuration describes
   */
  public static IdentityBinding fromConfiguredAlias(String alias) {
    return alias == null || alias.isBlank() ? unbound() : to(alias);
  }

  /**
   * Whether this process is restricted to a single identity.
   *
   * @return true when bound
   */
  public boolean isBound() {
    return alias != null;
  }

  /**
   * The bound alias.
   *
   * @return the alias, or empty when unbound
   */
  public Optional<String> alias() {
    return Optional.ofNullable(alias);
  }

  /**
   * Narrow a set of candidate aliases to those this process may unlock.
   *
   * <p>Applied before decryption rather than after, so a bound process's restriction is enforced
   * by never reading the other keys instead of by discarding them once read.
   *
   * @param candidates the aliases the keystore offers
   * @return every candidate when unbound, otherwise only the bound alias
   */
  public Set<String> permitted(@NonNull Set<String> candidates) {
    return isBound() ? Set.of(alias) : candidates;
  }

  @Override
  public String toString() {
    return isBound() ? "bound to '" + alias + "'" : "unbound";
  }
}
