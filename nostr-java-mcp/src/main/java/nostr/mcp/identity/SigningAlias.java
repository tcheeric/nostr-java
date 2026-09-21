package nostr.mcp.identity;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.mcp.tool.ToolFailure;

import java.util.Optional;

/**
 * Chooses which identity signs, refusing to guess.
 *
 * <p>Posting as the wrong account is public and irreversible, so where several identities exist
 * and none is the default, the caller is asked rather than picked for.
 *
 * <p>Separate from {@link nostr.mcp.identity.IdentityVault} because the vault answers what it
 * holds, and separate from {@code WriteGuard} because not everything that signs is a write: a
 * Blossom listing needs a signed token to read, and a read-only server must still be able to
 * make one. Both paths resolve the alias the same way, and a second copy of these messages
 * would be a second place for them to drift.
 */
public final class SigningAlias {

  private SigningAlias() {}

  /**
   * Resolve the alias to sign as.
   *
   * @param identityVault the keys this server holds
   * @param requestedAlias the identity named by the caller, or empty to use the default
   * @return the alias to sign as
   * @throws nostr.mcp.tool.ToolException when the alias is unknown, or none was given and no
   *     default exists
   */
  public static String resolve(
      @NonNull IdentityVault identityVault, @NonNull Optional<String> requestedAlias) {
    if (requestedAlias.isPresent()) {
      String alias = requestedAlias.get();
      if (identityVault.find(alias).isEmpty()) {
        throw ToolFailure.IDENTITY_UNKNOWN.raise(
            "No identity called '"
                + alias
                + "'. Available: "
                + identityVault.list().stream().map(IdentitySummary::alias).toList());
      }
      return alias;
    }
    return identityVault
        .defaultAlias()
        .orElseThrow(
            () ->
                ToolFailure.IDENTITY_AMBIGUOUS.raise(
                    identityVault.isEmpty()
                        ? "This server holds no identity to sign with. Create one with the"
                            + " command line: java -jar nostr-java-mcp.jar keygen <alias>"
                        : "This server holds several identities and none is the default, so"
                            + " signing would be a guess. Name one in the 'identity' argument:"
                            + identityVault.list().stream().map(IdentitySummary::alias).toList()));
  }

  /**
   * Stamp an event with an identity's public key and sign it.
   *
   * <p>The order matters and is easy to get wrong: the public key has to be set and the event
   * re-serialised before signing, or the signature covers an event id that does not match the
   * event that gets sent.
   *
   * @param identityVault the keys this server holds
   * @param alias the identity to sign as
   * @param event the unsigned event, which is mutated
   * @return the same event, now signed
   */
  public static GenericEvent sign(
      @NonNull IdentityVault identityVault, @NonNull String alias, @NonNull GenericEvent event) {
    PublicKey publicKey = identityVault.publicKeyOf(alias);
    event.setPubKey(publicKey);
    event.update();
    identityVault.signAs(alias, event);
    return event;
  }
}
