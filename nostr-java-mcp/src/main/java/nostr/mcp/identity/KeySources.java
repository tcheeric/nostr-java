package nostr.mcp.identity;

import lombok.NonNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Chooses a key source from configuration.
 *
 * <p>One place decides which backend a {@code keystore.type} means, so the vault never branches
 * on it and a new backend is a new case here rather than a change spread across startup.
 */
public final class KeySources {

  private static final String PASSPHRASE_VARIABLE = "NOSTR_MCP_KEYSTORE_PASSPHRASE";

  private KeySources() {}

  /**
   * Build the configured source.
   *
   * @param type the {@code keystore.type} value
   * @param keystorePath where an encrypted-file keystore lives
   * @param aliases the identities to look for, for backends that cannot enumerate themselves
   * @return the source to load keys from
   * @throws KeystoreException when the type is unknown or its prerequisites are missing
   */
  public static KeySource forType(
      @NonNull String type, @NonNull String keystorePath, @NonNull List<String> aliases) {
    return switch (type) {
      case OsKeychainKeySource.TYPE -> new OsKeychainKeySource(aliases);
      case EnvironmentKeySource.TYPE -> new EnvironmentKeySource();
      case EncryptedFileKeySource.TYPE ->
          new EncryptedFileKeySource(Path.of(keystorePath), requirePassphrase());
      default ->
          throw new KeystoreException(
              "Unknown keystore.type '"
                  + type
                  + "'; expected one of "
                  + List.of(
                      OsKeychainKeySource.TYPE,
                      EncryptedFileKeySource.TYPE,
                      EnvironmentKeySource.TYPE));
    };
  }

  /**
   * Reads the passphrase from the environment.
   *
   * <p>An encrypted keystore with no passphrase is a file with a lock and no key, so a missing
   * one is refused rather than defaulted. The message names the variable, because a server an
   * MCP host launches has nowhere to prompt.
   */
  private static char[] requirePassphrase() {
    String passphrase = System.getenv(PASSPHRASE_VARIABLE);
    if (passphrase == null || passphrase.isBlank()) {
      throw new KeystoreException(
          "The " + EncryptedFileKeySource.TYPE + " keystore needs a passphrase; set "
              + PASSPHRASE_VARIABLE);
    }
    return passphrase.toCharArray();
  }
}
