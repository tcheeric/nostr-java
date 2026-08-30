package nostr.mcp.identity;

import java.util.Map;

/**
 * Where signing keys come from.
 *
 * <p>Holding keys locally makes <em>where</em> the central security decision of this module, and
 * the answer differs by deployment: a desktop has a keychain, a container has a mounted file,
 * a test has neither. This interface is the seam between those, so adding a remote signer later
 * means adding an implementation rather than reworking the vault.
 *
 * <p>An implementation returns raw key material, which the {@link IdentityVault} takes ownership
 * of and wipes. Returning {@code byte[]} rather than a string is deliberate: a string cannot be
 * cleared and may be interned, so a leaked heap dump keeps the key indefinitely.
 */
public interface KeySource {

  /**
   * Read the keys this source holds that the binding permits.
   *
   * <p>Called once at startup. The caller wipes the returned arrays after use, so an
   * implementation must not retain them.
   *
   * <p>The binding is applied <em>before</em> decryption, not after. A bound process must never
   * hold another identity's key even briefly, so an implementation filters by alias while the
   * other entries are still encrypted rather than reading everything and discarding the rest.
   *
   * @param binding which identities this process may unlock
   * @return private key material by alias, empty when the source holds nothing permitted
   * @throws KeystoreException if the source exists but could not be read
   */
  Map<String, byte[]> loadKeys(IdentityBinding binding);

  /**
   * How this source identifies itself in logs and errors.
   *
   * @return the configuration value that selects it
   */
  String type();

  /**
   * Whether this source is safe outside development.
   *
   * <p>A source that answers {@code false} is announced at startup, on the principle that a
   * weaker choice should be noisy rather than silent.
   *
   * @return true when the source protects keys at rest
   */
  default boolean protectsKeysAtRest() {
    return true;
  }
}
