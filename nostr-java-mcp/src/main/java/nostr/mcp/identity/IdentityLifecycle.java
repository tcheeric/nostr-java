package nostr.mcp.identity;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PrivateKey;
import nostr.id.Identity;
import nostr.mcp.tool.ToolFailure;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.spec.SecretKeySpec;

/**
 * Creating, importing, renaming, exporting and destroying identities.
 *
 * <p>The vault holds keys for the running process and the store holds them across restarts, so
 * every lifecycle change has to touch both or leave the server disagreeing with its own keystore
 * after a restart. Doing that in one place is what keeps the two consistent, and it gives the
 * tools a vocabulary in terms of the lifecycle rather than of storage.
 *
 * <p>Which backups exist is tracked here too, because removal depends on it: destroying a key
 * that was never backed up is the one action in this module with no remedy at all.
 */
@Slf4j
public final class IdentityLifecycle {

  private static final String KEY_ALGORITHM = "AES";
  private static final Set<PosixFilePermission> OWNER_ONLY =
      Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);

  private final IdentityVault vault;
  private final IdentityStore store;
  private final Set<String> aliasesWithBackups = new HashSet<>();

  /**
   * @param vault the keys this process holds
   * @param store where keys persist across restarts
   */
  public IdentityLifecycle(@NonNull IdentityVault vault, @NonNull IdentityStore store) {
    this.vault = vault;
    this.store = store;
  }

  /**
   * Generate a new identity.
   *
   * @param alias the name to file it under
   * @return the new identity's public details
   * @throws nostr.mcp.tool.ToolException when the alias is taken or unusable
   */
  public IdentitySummary create(@NonNull String alias) {
    String validated = IdentityAlias.validated(alias);
    byte[] keyMaterial = randomKeyMaterial();
    try {
      persist(validated, keyMaterial);
      return summaryOf(validated);
    } finally {
      Arrays.fill(keyMaterial, (byte) 0);
    }
  }

  /**
   * Import an existing key from wherever the source names.
   *
   * @param alias the name to file it under
   * @param source where the server should read the key from
   * @return the imported identity's public details
   * @throws nostr.mcp.tool.ToolException when the alias is taken or the source is unreadable
   */
  public IdentitySummary importFrom(@NonNull String alias, @NonNull String source) {
    String validated = IdentityAlias.validated(alias);
    byte[] keyMaterial = KeyImportSource.read(source);
    try {
      persist(validated, keyMaterial);
      return summaryOf(validated);
    } finally {
      Arrays.fill(keyMaterial, (byte) 0);
    }
  }

  /**
   * Rename an identity in both the vault and the store.
   *
   * @param currentAlias the existing name
   * @param newAlias the name to use instead
   * @return the renamed identity's public details
   */
  public IdentitySummary rename(@NonNull String currentAlias, @NonNull String newAlias) {
    String validated = IdentityAlias.validated(newAlias);
    byte[] keyMaterial = keyMaterialFor(currentAlias);
    try {
      store.store(validated, keyMaterial);
      store.remove(currentAlias);
      vault.rename(currentAlias, validated);
      moveBackupRecord(currentAlias, validated);
      return summaryOf(validated);
    } catch (KeystoreException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(e.getMessage());
    } finally {
      Arrays.fill(keyMaterial, (byte) 0);
    }
  }

  /**
   * Write a passphrase-encrypted backup and report where it went.
   *
   * @param alias the identity to back up
   * @param destination where to write the file
   * @param passphrase what protects it
   * @return the path written
   * @throws nostr.mcp.tool.ToolException when the file could not be written
   */
  public Path exportBackup(@NonNull String alias, @NonNull Path destination, @NonNull char[] passphrase) {
    byte[] keyMaterial = keyMaterialFor(alias);
    try {
      writeEncryptedBackup(alias, destination, passphrase, keyMaterial);
      aliasesWithBackups.add(alias);
      log.info(
          "Exported a backup of identity '{}' ({}) to {}",
          alias,
          vault.publicKeyOf(alias).toBech32String(),
          destination);
      return destination;
    } finally {
      Arrays.fill(keyMaterial, (byte) 0);
      Arrays.fill(passphrase, '\0');
    }
  }

  /**
   * Destroy an identity, in memory and on disk.
   *
   * @param alias the identity to remove
   */
  public void remove(@NonNull String alias) {
    vault.remove(alias);
    store.remove(alias);
    aliasesWithBackups.remove(alias);
  }

  /**
   * Whether a backup of this identity has been exported since the server started.
   *
   * <p>Deliberately not persisted. A record claiming a backup exists is worthless unless the file
   * still does, and this server cannot know that after a restart; the honest answer is then "no
   * backup I know of", which makes removal ask for an explicit acknowledgement rather than
   * relying on a stale reassurance.
   *
   * @param alias the identity to check
   * @return true when a backup was taken in this session
   */
  public boolean hasBackup(@NonNull String alias) {
    return aliasesWithBackups.contains(alias);
  }

  private void persist(String alias, byte[] keyMaterial) {
    try {
      store.store(alias, keyMaterial);
    } catch (KeystoreException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(e.getMessage());
    }
    try {
      vault.add(alias, keyMaterial.clone());
    } catch (KeystoreException e) {
      store.remove(alias);
      throw ToolFailure.INVALID_ARGUMENT.raise(e.getMessage());
    }
  }

  /**
   * Recovers the key material for an identity the vault already holds.
   *
   * <p>Derived from the vault rather than read back from the store, so this works for a backend
   * that cannot re-read what it wrote, and so an export never depends on the keystore being
   * readable a second time.
   */
  private byte[] keyMaterialFor(String alias) {
    return vault.exportKeyMaterial(alias);
  }

  private byte[] randomKeyMaterial() {
    return java.util.HexFormat.of()
        .parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString());
  }

  private IdentitySummary summaryOf(String alias) {
    return vault.find(alias).orElseThrow(() -> new IdentityUnknownException(alias, Set.of()));
  }

  private void moveBackupRecord(String currentAlias, String newAlias) {
    if (aliasesWithBackups.remove(currentAlias)) {
      aliasesWithBackups.add(newAlias);
    }
  }

  /**
   * Writes the backup as a keystore of its own, so restoring it needs no bespoke format.
   *
   * <p>Owner-only permissions are set before anything sensitive is written, since a backup
   * readable by other users is a copy of the key an attacker can work on offline.
   */
  private void writeEncryptedBackup(
      String alias, Path destination, char[] passphrase, byte[] keyMaterial) {
    try {
      Path parent = destination.toAbsolutePath().getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      KeyStore backup = KeyStore.getInstance("PKCS12");
      backup.load(null, passphrase);
      backup.setEntry(
          alias,
          new KeyStore.SecretKeyEntry(new SecretKeySpec(keyMaterial, KEY_ALGORITHM)),
          new KeyStore.PasswordProtection(passphrase));
      try (OutputStream file = Files.newOutputStream(destination)) {
        restrictToOwner(destination);
        backup.store(file, passphrase);
      }
      restrictToOwner(destination);
    } catch (Exception e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "Could not write the backup to " + destination + ": " + e.getMessage());
    }
  }

  private void restrictToOwner(Path file) throws IOException {
    if (file.getFileSystem().supportedFileAttributeViews().contains("posix")) {
      Files.setPosixFilePermissions(file, OWNER_ONLY);
    }
  }
}
