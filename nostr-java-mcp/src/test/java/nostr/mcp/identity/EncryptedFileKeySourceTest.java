package nostr.mcp.identity;

import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the portable keystore against a real file.
 *
 * <p>Exercised through the real {@code PKCS12} implementation rather than a stand-in, because the
 * things most likely to be wrong here, which algorithms the format accepts and which permissions
 * the file ends up with, are precisely the things a fake would have to invent.
 */
class EncryptedFileKeySourceTest {

  private static final char[] PASSPHRASE = "correct horse battery staple".toCharArray();

  @TempDir Path directory;

  // Verifies a stored key can be read back byte for byte, which is the backend's entire job.
  @Test
  void aStoredKeyIsReadBackUnchanged() {
    byte[] key = randomKey();
    EncryptedFileKeySource source = source();

    source.store("personal", key);

    assertArrayEquals(key, source().loadKeys(IdentityBinding.unbound()).get("personal"));
  }

  // Verifies a missing keystore is an empty one rather than an error, since a first run has no
  // file yet and should still start.
  @Test
  void aMissingKeystoreHoldsNothing() {
    assertTrue(source().loadKeys(IdentityBinding.unbound()).isEmpty());
    assertTrue(source().aliases().isEmpty());
  }

  // Verifies a bound process decrypts only its own entry, so another identity's key never
  // enters the process at all.
  @Test
  void aBoundProcessReadsOnlyItsOwnEntry() {
    EncryptedFileKeySource source = source();
    source.store("personal", randomKey());
    source.store("project-bot", randomKey());

    Map<String, byte[]> keys = source().loadKeys(IdentityBinding.to("personal"));

    assertEquals(Set.of("personal"), keys.keySet());
  }

  // Verifies the aliases can be listed without decrypting, which is what lets a bound process
  // know what exists while reading only its own key.
  @Test
  void aliasesAreListedWithoutDecrypting() {
    EncryptedFileKeySource source = source();
    source.store("personal", randomKey());
    source.store("project-bot", randomKey());

    assertEquals(List.of("personal", "project-bot"), source().aliases());
  }

  // Verifies an existing alias is never silently replaced, since overwriting a key destroys the
  // account it belonged to.
  @Test
  void anExistingAliasIsNotOverwritten() {
    byte[] original = randomKey();
    source().store("personal", original);

    assertThrows(KeystoreException.class, () -> source().store("personal", randomKey()));
    assertArrayEquals(original, source().loadKeys(IdentityBinding.unbound()).get("personal"));
  }

  // Verifies removal really removes, and that removing an absent alias is reported rather than
  // silently succeeding.
  @Test
  void removalIsReportedHonestly() {
    source().store("personal", randomKey());

    assertTrue(source().remove("personal"));
    assertFalse(source().remove("personal"));
    assertTrue(source().loadKeys(IdentityBinding.unbound()).isEmpty());
  }

  // Verifies the keystore is created readable only by its owner, since the passphrase is the
  // only other protection it has.
  @Test
  void theKeystoreIsCreatedReadableOnlyByItsOwner() throws IOException {
    source().store("personal", randomKey());

    assertEquals(
        Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
        Files.getPosixFilePermissions(keystorePath()));
  }

  // Verifies a keystore other users can read is refused rather than warned about, because a file
  // an attacker can copy is one they can attack offline for as long as they like.
  @Test
  void aWorldReadableKeystoreIsRefused() throws IOException {
    source().store("personal", randomKey());
    Files.setPosixFilePermissions(
        keystorePath(),
        Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OTHERS_READ));

    KeystoreException refused =
        assertThrows(
            KeystoreException.class, () -> source().loadKeys(IdentityBinding.unbound()));

    assertTrue(refused.getMessage().contains("chmod 600"), refused.getMessage());
  }

  // Verifies a wrong passphrase says so, rather than reporting an empty keystore and letting a
  // server start with no identities for no visible reason.
  @Test
  void aWrongPassphraseIsReportedClearly() {
    source().store("personal", randomKey());

    EncryptedFileKeySource wrong =
        new EncryptedFileKeySource(keystorePath(), "wrong".toCharArray());

    KeystoreException failure =
        assertThrows(KeystoreException.class, () -> wrong.loadKeys(IdentityBinding.unbound()));

    assertTrue(failure.getMessage().contains("passphrase"), failure.getMessage());
  }

  private EncryptedFileKeySource source() {
    return new EncryptedFileKeySource(keystorePath(), PASSPHRASE);
  }

  private Path keystorePath() {
    return directory.resolve("keys.p12");
  }

  private byte[] randomKey() {
    return HexFormat.of()
        .parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString());
  }
}
