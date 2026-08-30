package nostr.mcp.identity;

import lombok.NonNull;
import nostr.base.PublicKey;

/**
 * What an agent is allowed to learn about an identity.
 *
 * <p>This type is the enforcement mechanism for "no tool can return a private key", not a
 * convenience. It has no field capable of holding key material, so a tool cannot leak one by
 * accident, by refactoring, or by a future author not knowing the rule. Making the guarantee a
 * property of the type means it survives people.
 *
 * @param alias the human-meaningful name this identity is known by
 * @param publicKey the identity's public key, in hex
 * @param npub the same key in bech32 form, which is what a user recognises
 */
public record IdentitySummary(String alias, String publicKey, String npub) {

  /**
   * Describe an identity by its alias and public key.
   *
   * @param alias the name this identity is known by
   * @param publicKey the public key to report
   * @return the summary an agent may see
   */
  public static IdentitySummary of(@NonNull String alias, @NonNull PublicKey publicKey) {
    return new IdentitySummary(alias, publicKey.toHexString(), publicKey.toBech32String());
  }
}
