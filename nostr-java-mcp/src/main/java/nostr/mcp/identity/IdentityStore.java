package nostr.mcp.identity;

import java.util.List;

/**
 * Creating, importing and removing keys.
 *
 * <p>Separate from {@link KeySource} because reading keys and administering them are different
 * privileges held by different actors: every server reads, only a human administers. A backend
 * that cannot write, such as a future remote signer, implements the source alone and is not
 * silently broken by a lifecycle it cannot honour.
 *
 * <p>The CLI and the identity lifecycle tools both drive this interface, so there is one
 * implementation of what "remove an identity" means and two front doors to it.
 */
public interface IdentityStore {

  /**
   * Store a key under an alias.
   *
   * @param alias the name to file it under
   * @param keyMaterial the private key bytes, which the caller owns and wipes
   * @throws KeystoreException when the alias is taken or the store could not be written
   */
  void store(String alias, byte[] keyMaterial);

  /**
   * The aliases this store holds, without decrypting anything.
   *
   * @return the known aliases, in insertion order where the backend preserves it
   */
  List<String> aliases();

  /**
   * Forget a key.
   *
   * @param alias the identity to remove
   * @return true when an entry was removed, false when there was none
   * @throws KeystoreException when the store could not be written
   */
  boolean remove(String alias);
}
