package nostr.mcp.identity;

import nostr.id.Identity;
import nostr.mcp.tool.ToolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the keystore and the running vault stay in step through every lifecycle change. */
class IdentityLifecycleTest {

  @TempDir Path directory;

  private final RecordingStore store = new RecordingStore();

  // Verifies a created identity is usable immediately and survives a restart, which needs both
  // the vault and the store to have been updated.
  @Test
  void aCreatedIdentityIsInBothTheVaultAndTheStore() {
    try (IdentityVault vault = emptyVault()) {
      IdentitySummary created = new IdentityLifecycle(vault, store).create("personal");

      assertEquals("personal", created.alias());
      assertTrue(created.npub().startsWith("npub"));
      assertTrue(vault.find("personal").isPresent(), "the vault cannot sign with it");
      assertEquals(List.of("personal"), store.aliases(), "it would vanish on restart");
    }
  }

  // Verifies a created identity returns no private key, since an agent should be able to create
  // an account it can use but cannot leak.
  @Test
  void creatingReturnsNoPrivateKey() {
    try (IdentityVault vault = emptyVault()) {
      IdentitySummary created = new IdentityLifecycle(vault, store).create("personal");

      String privateKeyHex = HexFormat.of().formatHex(store.keyFor("personal"));
      assertFalse(created.toString().contains(privateKeyHex), created.toString());
      assertEquals(3, IdentitySummary.class.getRecordComponents().length);
    }
  }

  // Verifies an unusable alias is refused, since aliases appear in resource URIs where a slash
  // or a space would address something else entirely.
  @Test
  void anAliasThatWouldBreakAUriIsRefused() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, store);

      assertThrows(ToolException.class, () -> lifecycle.create("has spaces"));
      assertThrows(ToolException.class, () -> lifecycle.create("has/slash"));
      assertThrows(ToolException.class, () -> lifecycle.create("UPPER"));
      assertThrows(ToolException.class, () -> lifecycle.create(""));
      assertTrue(store.aliases().isEmpty());
    }
  }

  // Verifies a key is imported from a file the server reads itself, which is how an import
  // happens without the key passing through the model.
  @Test
  void aKeyIsImportedFromAFileTheServerReads() throws Exception {
    Path keyFile = directory.resolve("key.txt");
    String keyHex = Identity.generateRandomIdentity().getPrivateKey().toHexString();
    Files.writeString(keyFile, keyHex);

    try (IdentityVault vault = emptyVault()) {
      IdentitySummary imported =
          new IdentityLifecycle(vault, store).importFrom("personal", "file:" + keyFile);

      assertEquals(
          Identity.create(new nostr.base.PrivateKey(HexFormat.of().parseHex(keyHex)))
              .getPublicKey()
              .toHexString(),
          imported.publicKey());
    }
  }

  // Verifies key material given as the source is refused with advice to rotate, since by then
  // the key is already in the conversation.
  @Test
  void keyMaterialAsTheSourceIsRefusedWithAdvice() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, store);
      String nsec = Identity.generateRandomIdentity().getPrivateKey().toBech32String();

      ToolException refused =
          assertThrows(ToolException.class, () -> lifecycle.importFrom("personal", nsec));

      assertTrue(refused.getMessage().contains("compromised"), refused.getMessage());
      assertTrue(store.aliases().isEmpty());
    }
  }

  // Verifies renaming keeps the same key, so the account is untouched and only the local label
  // changes.
  @Test
  void renamingKeepsTheSameKey() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, store);
      String publicKey = lifecycle.create("old-name").publicKey();

      IdentitySummary renamed = lifecycle.rename("old-name", "new-name");

      assertEquals(publicKey, renamed.publicKey());
      assertEquals(List.of("new-name"), store.aliases());
      assertTrue(vault.find("old-name").isEmpty());
    }
  }

  // Verifies a backup is written encrypted and readable only by its owner, since a backup is a
  // copy of the key and a readable one is a key an attacker can take away.
  @Test
  void aBackupIsEncryptedAndOwnerOnly() throws Exception {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, store);
      lifecycle.create("personal");
      Path backup = directory.resolve("backup.p12");

      lifecycle.exportBackup("personal", backup, "passphrase".toCharArray());

      assertTrue(Files.exists(backup));
      assertEquals(
          Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
          Files.getPosixFilePermissions(backup));
      String contents = new String(Files.readAllBytes(backup), java.nio.charset.StandardCharsets.ISO_8859_1);
      assertFalse(
          contents.contains(HexFormat.of().formatHex(store.keyFor("personal"))),
          "the key is in the backup in plaintext");
    }
  }

  // Verifies exporting a backup is what makes removal permissible, since removal without one is
  // the single unrecoverable action in the module.
  @Test
  void aBackupIsRecordedSoRemovalKnowsAboutIt() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, store);
      lifecycle.create("personal");

      assertFalse(lifecycle.hasBackup("personal"));
      lifecycle.exportBackup("personal", directory.resolve("b.p12"), "pass".toCharArray());
      assertTrue(lifecycle.hasBackup("personal"));
    }
  }

  // Verifies removal clears the identity from the vault and the store, so it cannot sign now and
  // does not reappear after a restart.
  @Test
  void removalClearsBothTheVaultAndTheStore() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, store);
      lifecycle.create("personal");

      lifecycle.remove("personal");

      assertTrue(vault.find("personal").isEmpty());
      assertTrue(store.aliases().isEmpty());
    }
  }

  // Verifies a taken alias is refused rather than silently replacing an existing key, which
  // would destroy an account with no warning.
  @Test
  void anExistingAliasIsNotOverwritten() {
    try (IdentityVault vault = emptyVault()) {
      IdentityLifecycle lifecycle = new IdentityLifecycle(vault, store);
      String original = lifecycle.create("personal").publicKey();

      assertThrows(ToolException.class, () -> lifecycle.create("personal"));
      assertEquals(original, vault.publicKeyOf("personal").toHexString());
    }
  }

  private IdentityVault emptyVault() {
    return new IdentityVault(
        new KeySource() {
          @Override
          public Map<String, byte[]> loadKeys(IdentityBinding binding) {
            return Map.of();
          }

          @Override
          public String type() {
            return "test";
          }
        },
        null);
  }

  /** A store that records what it was told, standing in for a keystore file. */
  private static final class RecordingStore implements IdentityStore {
    private final Map<String, byte[]> keys = new LinkedHashMap<>();

    @Override
    public void store(String alias, byte[] keyMaterial) {
      if (keys.containsKey(alias)) {
        throw new KeystoreException("already holds '" + alias + "'");
      }
      keys.put(alias, keyMaterial.clone());
    }

    @Override
    public List<String> aliases() {
      return new ArrayList<>(keys.keySet());
    }

    @Override
    public boolean remove(String alias) {
      return keys.remove(alias) != null;
    }

    byte[] keyFor(String alias) {
      return keys.get(alias);
    }
  }
}
