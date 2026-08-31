package nostr.mcp.identity;

import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Keys in a passphrase-protected keystore file.
 *
 * <p>The portable option, and the right one in a container: it needs no platform service, and it
 * survives a restart through a mounted volume. It is also the only backend a deployment fully
 * controls, which is why it carries the permission check.
 *
 * <p>A world-readable keystore is refused rather than warned about. The passphrase is the only
 * thing protecting the file, and a file every user on the host can copy is one an attacker can
 * work on offline for as long as they like.
 */
public final class EncryptedFileKeySource implements KeySource, IdentityStore {

  /** The value that selects this backend. */
  public static final String TYPE = "encrypted-file";

  /**
   * PKCS#12 stores a secret key under a named algorithm and rejects one it does not recognise,
   * so a nostr key travels as an AES key of the same 32 bytes. The name is a container label
   * here, not a statement about how the key is used: nothing ever asks this keystore to encrypt.
   */
  private static final String KEY_ALGORITHM = "AES";
  private static final Set<PosixFilePermission> FORBIDDEN_PERMISSIONS =
      Set.of(
          PosixFilePermission.GROUP_READ,
          PosixFilePermission.GROUP_WRITE,
          PosixFilePermission.OTHERS_READ,
          PosixFilePermission.OTHERS_WRITE);

  private final Path keystorePath;
  private final char[] passphrase;

  /**
   * @param keystorePath the keystore file
   * @param passphrase the passphrase protecting it
   */
  public EncryptedFileKeySource(@NonNull Path keystorePath, @NonNull char[] passphrase) {
    this.keystorePath = keystorePath;
    this.passphrase = passphrase.clone();
  }

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public Map<String, byte[]> loadKeys(IdentityBinding binding) {
    if (!Files.exists(keystorePath)) {
      return Map.of();
    }
    refuseIfReadableByOthers();
    try (InputStream keystoreStream = Files.newInputStream(keystorePath)) {
      return readEntries(loadKeystore(keystoreStream), binding);
    } catch (IOException e) {
      throw new KeystoreException("Could not read the keystore at " + keystorePath, e);
    }
  }

  /**
   * Write a key into the keystore, creating it if needed.
   *
   * <p>Belongs here rather than in a separate writer because the file's permissions and its
   * passphrase are the same concern as reading it, and splitting them invites a writer that
   * creates the very world-readable file the reader refuses.
   *
   * @param alias the name to store it under
   * @param keyMaterial the private key bytes
   */
  @Override
  public void store(@NonNull String alias, @NonNull byte[] keyMaterial) {
    try {
      KeyStore keystore = openOrCreate();
      refuseIfAliasTaken(keystore, alias);
      keystore.setEntry(
          alias,
          new KeyStore.SecretKeyEntry(new SecretKeySpec(keyMaterial, KEY_ALGORITHM)),
          new KeyStore.PasswordProtection(passphrase));
      writeOwnerOnly(keystore);
    } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
      throw new KeystoreException("Could not store the key for '" + alias + "'", e);
    }
  }

  @Override
  public List<String> aliases() {
    if (!Files.exists(keystorePath)) {
      return List.of();
    }
    refuseIfReadableByOthers();
    try (InputStream keystoreStream = Files.newInputStream(keystorePath)) {
      return Collections.list(loadKeystore(keystoreStream).aliases());
    } catch (IOException | KeyStoreException e) {
      throw new KeystoreException("Could not list the keystore at " + keystorePath, e);
    }
  }

  @Override
  public boolean remove(@NonNull String alias) {
    if (!Files.exists(keystorePath)) {
      return false;
    }
    try {
      KeyStore keystore = openOrCreate();
      if (!keystore.containsAlias(alias)) {
        return false;
      }
      keystore.deleteEntry(alias);
      writeOwnerOnly(keystore);
      return true;
    } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
      throw new KeystoreException("Could not remove the key for '" + alias + "'", e);
    }
  }

  /**
   * Refuses to overwrite an existing entry.
   *
   * <p>Silently replacing a key destroys an account with no warning and no way back, so the
   * caller is told to remove it deliberately first.
   */
  private void refuseIfAliasTaken(KeyStore keystore, String alias) throws KeyStoreException {
    if (keystore.containsAlias(alias)) {
      throw new KeystoreException(
          "The keystore already holds an identity called '"
              + alias
              + "'; remove it first if you really mean to replace it");
    }
  }

  private KeyStore openOrCreate() throws KeyStoreException, IOException, NoSuchAlgorithmException,
      CertificateException {
    KeyStore keystore = KeyStore.getInstance("PKCS12");
    if (Files.exists(keystorePath)) {
      try (InputStream keystoreStream = Files.newInputStream(keystorePath)) {
        keystore.load(keystoreStream, passphrase);
      }
    } else {
      keystore.load(null, passphrase);
    }
    return keystore;
  }

  private void writeOwnerOnly(KeyStore keystore) throws IOException, KeyStoreException,
      NoSuchAlgorithmException, CertificateException {
    Path parent = keystorePath.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    try (OutputStream keystoreStream = Files.newOutputStream(keystorePath)) {
      keystore.store(keystoreStream, passphrase);
    }
    restrictToOwner();
  }

  private void restrictToOwner() throws IOException {
    if (supportsPosixPermissions()) {
      Files.setPosixFilePermissions(
          keystorePath,
          Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
    }
  }

  private KeyStore loadKeystore(InputStream keystoreStream) {
    try {
      KeyStore keystore = KeyStore.getInstance("PKCS12");
      keystore.load(keystoreStream, passphrase);
      return keystore;
    } catch (IOException e) {
      throw new KeystoreException(
          "Could not open the keystore at " + keystorePath + "; the passphrase may be wrong", e);
    } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
      throw new KeystoreException("Could not open the keystore at " + keystorePath, e);
    }
  }

  /**
   * Decrypts only the permitted entries.
   *
   * <p>Listing aliases does not decrypt anything, so a bound process can see which entries exist
   * and still read only its own. That ordering is the guarantee: the other keys stay ciphertext.
   */
  private Map<String, byte[]> readEntries(KeyStore keystore, IdentityBinding binding) {
    Map<String, byte[]> keys = new LinkedHashMap<>();
    try {
      Set<String> permitted = binding.permitted(new LinkedHashSet<>(Collections.list(keystore.aliases())));
      for (String alias : Collections.list(keystore.aliases())) {
        if (permitted.contains(alias)) {
          keys.put(alias, readEntry(keystore, alias));
        }
      }
    } catch (KeyStoreException e) {
      throw new KeystoreException("Could not list the keystore entries", e);
    }
    return keys;
  }

  private byte[] readEntry(KeyStore keystore, String alias) {
    try {
      KeyStore.Entry entry =
          keystore.getEntry(alias, new KeyStore.PasswordProtection(passphrase));
      if (!(entry instanceof KeyStore.SecretKeyEntry secretKeyEntry)) {
        throw new KeystoreException("Keystore entry '" + alias + "' is not a private key");
      }
      SecretKey key = secretKeyEntry.getSecretKey();
      return decodeIfHex(key.getEncoded());
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
      throw new KeystoreException("Could not read the keystore entry '" + alias + "'", e);
    }
  }

  /**
   * Accepts a key stored either as raw bytes or as the hex text a person would paste.
   *
   * <p>A keystore written by the CLI holds raw bytes, but one a user populated by hand may hold
   * the hex form. Failing on the second would be a confusing way to say "wrong format".
   */
  private byte[] decodeIfHex(byte[] stored) {
    if (stored.length != 64) {
      return stored;
    }
    try {
      return HexFormat.of().parseHex(new String(stored, StandardCharsets.US_ASCII));
    } catch (IllegalArgumentException notHex) {
      return stored;
    }
  }

  private void refuseIfReadableByOthers() {
    if (!supportsPosixPermissions()) {
      return;
    }
    try {
      Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(keystorePath);
      Set<PosixFilePermission> exposed = new LinkedHashSet<>(permissions);
      exposed.retainAll(FORBIDDEN_PERMISSIONS);
      if (!exposed.isEmpty()) {
        throw new KeystoreException(
            "Refusing to read the keystore at "
                + keystorePath
                + " because it is readable beyond its owner ("
                + exposed
                + "); run chmod 600 on it");
      }
    } catch (IOException e) {
      throw new KeystoreException("Could not check the keystore's permissions", e);
    }
  }

  private boolean supportsPosixPermissions() {
    return keystorePath.getFileSystem().supportedFileAttributeViews().contains("posix");
  }

  /** Wipes the passphrase copy this source holds. */
  public void close() {
    Arrays.fill(passphrase, '\0');
  }
}
