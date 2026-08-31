package nostr.mcp.identity;

/**
 * Thrown when a keystore exists but cannot be used.
 *
 * <p>Distinct from an empty keystore, which is an ordinary state a first-run server reports and
 * explains. This means the keys are there and something is wrong: a bad passphrase, unsafe file
 * permissions, an unreadable platform keychain. The server refuses to start rather than
 * continuing without the identities it was configured with, since silently signing as nobody is
 * worse than not starting.
 */
public class KeystoreException extends RuntimeException {

  /**
   * @param message what could not be read, and why
   */
  public KeystoreException(String message) {
    super(message);
  }

  /**
   * @param message what could not be read, and why
   * @param cause the underlying failure
   */
  public KeystoreException(String message, Throwable cause) {
    super(message, cause);
  }
}
