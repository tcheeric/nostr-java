package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.mcp.identity.IdentityBinding;
import nostr.mcp.identity.IdentityLifecycle;
import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.identity.IdentityStore;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.identity.KeystoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the one irreversible tool in the module is hard to trigger by accident.
 *
 * <p>An npub with no nsec is a dead account, so these tests are about what has to be true before
 * a key can be destroyed, not about the destruction itself.
 */
class RemoveIdentityToolTest {

  @TempDir Path directory;

  private IdentityVault vault;
  private IdentityLifecycle lifecycle;
  private RemoveIdentityTool tool;

  @BeforeEach
  void setUp() {
    vault = emptyVault();
    lifecycle = new IdentityLifecycle(vault, new InMemoryStore());
    lifecycle.create("personal");
    tool = new RemoveIdentityTool(lifecycle, vault, IdentityPolicy.CONFIRM);
  }

  // Verifies an unbacked-up key cannot be removed at all, since an agent acting on a vague
  // instruction has no basis for deciding an account is disposable.
  @Test
  void anUnbackedUpKeyIsRefusedOutright() {
    CallToolResult refused = call(Map.of("alias", "personal"));

    assertTrue(Boolean.TRUE.equals(refused.isError()), textOf(refused));
    assertTrue(textOf(refused).contains("No backup"), textOf(refused));
    assertTrue(vault.find("personal").isPresent(), "the identity was destroyed");
  }

  // Verifies the refusal names the way forward, so an agent can either back up or state that the
  // key is disposable rather than simply failing.
  @Test
  void theRefusalNamesBothWaysForward() {
    String message = textOf(call(Map.of("alias", "personal")));

    assertTrue(message.contains("nostr_export_identity_backup"), message);
    assertTrue(message.contains("acknowledgeNoBackup"), message);
  }

  // Verifies a backup makes removal possible, which is the whole reason the backup tool exists.
  @Test
  void aBackupUnlocksRemoval() {
    lifecycle.exportBackup("personal", directory.resolve("b.p12"), "pass".toCharArray());

    CallToolResult preview = call(Map.of("alias", "personal"));

    assertFalse(Boolean.TRUE.equals(preview.isError()), textOf(preview));
    assertTrue(textOf(preview).contains("Nothing has been deleted yet"), textOf(preview));
  }

  // Verifies an explicit acknowledgement also unlocks removal, for a key the user has said is
  // disposable, and that it still only previews rather than deleting at once.
  @Test
  void anAcknowledgementUnlocksRemovalButStillPreviews() {
    CallToolResult preview = call(Map.of("alias", "personal", "acknowledgeNoBackup", true));

    assertFalse(Boolean.TRUE.equals(preview.isError()), textOf(preview));
    assertTrue(vault.find("personal").isPresent(), "the first call deleted the key");
  }

  // Verifies the previewed key is destroyed only on the second call carrying the token.
  @Test
  void theSecondCallWithTheTokenDestroysTheKey() {
    CallToolResult preview = call(Map.of("alias", "personal", "acknowledgeNoBackup", true));
    String token = String.valueOf(structuredOf(preview).get("confirmationToken"));

    CallToolResult removed = call(Map.of("alias", "personal", "confirmationToken", token));

    assertFalse(Boolean.TRUE.equals(removed.isError()), textOf(removed));
    assertTrue(vault.find("personal").isEmpty(), "the key survived removal");
  }

  // Verifies an invented token destroys nothing, since a hallucinated confirmation must not be
  // able to end an account.
  @Test
  void anInventedTokenDestroysNothing() {
    CallToolResult refused = call(Map.of("alias", "personal", "confirmationToken", "invented"));

    assertTrue(Boolean.TRUE.equals(refused.isError()), textOf(refused));
    assertTrue(vault.find("personal").isPresent());
  }

  // Verifies the preview says whether a backup exists, so the agent can tell the user what is
  // at stake before they answer.
  @Test
  void thePreviewSaysWhetherABackupExists() {
    CallToolResult withoutBackup = call(Map.of("alias", "personal", "acknowledgeNoBackup", true));
    assertTrue(textOf(withoutBackup).contains("No backup has been exported"), textOf(withoutBackup));

    lifecycle.exportBackup("personal", directory.resolve("b.p12"), "pass".toCharArray());
    CallToolResult withBackup = call(Map.of("alias", "personal"));
    assertTrue(textOf(withBackup).contains("A backup was exported"), textOf(withBackup));
  }

  // Verifies removing an identity that does not exist is reported rather than silently accepted.
  @Test
  void removingSomethingThatDoesNotExistIsReported() {
    CallToolResult refused = call(Map.of("alias", "nobody", "acknowledgeNoBackup", true));

    assertTrue(Boolean.TRUE.equals(refused.isError()), textOf(refused));
    assertTrue(textOf(refused).startsWith("IDENTITY_UNKNOWN"), textOf(refused));
  }

  private CallToolResult call(Map<String, Object> arguments) {
    return tool.call(new CallToolRequest("nostr_remove_identity", arguments));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> structuredOf(CallToolResult result) {
    return (Map<String, Object>) result.structuredContent();
  }

  private String textOf(CallToolResult result) {
    return result.content().stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .map(TextContent::text)
        .findFirst()
        .orElse("");
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

  /** A store standing in for a keystore file. */
  private static final class InMemoryStore implements IdentityStore {
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
  }
}
