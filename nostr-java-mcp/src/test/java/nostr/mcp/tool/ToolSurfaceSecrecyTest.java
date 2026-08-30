package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import nostr.client.relay.FakeRelay;
import nostr.client.relay.RelayPool;
import nostr.id.Identity;
import nostr.mcp.identity.IdentitySummary;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.KeySource;
import nostr.mcp.relay.RelayDirectory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Walks the whole tool surface and asserts no private key can escape through it.
 *
 * <p>A key is the one thing here that cannot be un-leaked: an agent's context reaches the host's
 * conversation log and usually a third-party inference API, so a key that appears in any result
 * is a key that is gone. Testing each tool individually would leave the guarantee resting on
 * whoever adds the next one remembering, so this walks the registry instead and fails when a
 * tool is added that breaks the rule.
 */
class ToolSurfaceSecrecyTest {

  private static final String ALIAS = "personal";

  // Verifies no tool's successful result contains the private key of any identity the server
  // holds, which is the guarantee the whole vault design exists to provide.
  @Test
  void noToolResultContainsAPrivateKey() {
    String privateKeyHex = HexFormat.of().formatHex(KEY_MATERIAL);

    try (IdentityVault vault = vaultHoldingTheKey();
        RelayPool pool = poolOf("wss://relay.one")) {

      for (NostrTool tool : surfaceOf(vault, pool)) {
        CallToolResult result = tool.call(new CallToolRequest(tool.name(), Map.of()));

        assertFalse(
            result.toString().toLowerCase().contains(privateKeyHex.toLowerCase()),
            tool.name() + " returned the private key");
      }
    }
  }

  // Verifies no tool's input schema invites key material, since a tool that accepts an nsec puts
  // it in the model's context before the server ever sees it.
  @Test
  void noToolSchemaAcceptsKeyMaterial() {
    try (IdentityVault vault = vaultHoldingTheKey();
        RelayPool pool = poolOf("wss://relay.one")) {

      for (NostrTool tool : surfaceOf(vault, pool)) {
        String schema = tool.inputSchema().toString().toLowerCase();

        assertFalse(schema.contains("nsec"), tool.name() + " accepts an nsec");
        assertFalse(schema.contains("privatekey"), tool.name() + " accepts a private key");
        assertFalse(schema.contains("private_key"), tool.name() + " accepts a private key");
        assertFalse(schema.contains("secret"), tool.name() + " accepts a secret");
      }
    }
  }

  // Verifies the type an agent receives has no component capable of holding a key, so the
  // guarantee survives someone adding a field without reading this test.
  @Test
  void theIdentitySummaryTypeCannotHoldAKey() {
    List<String> components =
        Arrays.stream(IdentitySummary.class.getRecordComponents())
            .map(RecordComponent::getName)
            .toList();

    assertTrue(components.contains("alias"));
    assertTrue(components.contains("publicKey"));
    assertTrue(components.contains("npub"));
    assertFalse(
        components.stream().anyMatch(name -> name.toLowerCase().contains("private")),
        "IdentitySummary gained a component that could hold a key: " + components);
  }

  // Verifies an error naming an unknown identity lists aliases without disclosing keys, since a
  // failure path is as good a leak as a success path.
  @Test
  void anErrorAboutAnUnknownIdentityDisclosesNoKey() {
    String privateKeyHex = HexFormat.of().formatHex(KEY_MATERIAL);

    try (IdentityVault vault = vaultHoldingTheKey()) {
      String message =
          assertThrows(
                  RuntimeException.class, () -> vault.publicKeyOf("nobody"))
              .getMessage();

      assertTrue(message.contains(ALIAS), "the error should name the aliases that exist");
      assertFalse(message.toLowerCase().contains(privateKeyHex.toLowerCase()));
    }
  }

  private List<NostrTool> surfaceOf(IdentityVault vault, RelayPool pool) {
    return List.of(
        new ListIdentitiesTool(vault),
        new ListRelaysTool(
            new RelayDirectory(Map.of(RelayDirectory.READ, List.of("wss://relay.one"))), pool));
  }

  private IdentityVault vaultHoldingTheKey() {
    return new IdentityVault(
        new KeySource() {
          @Override
          public Map<String, byte[]> loadKeys() {
            return Map.of(ALIAS, KEY_MATERIAL.clone());
          }

          @Override
          public String type() {
            return "test";
          }
        },
        null);
  }

  private RelayPool poolOf(String relayUri) {
    return new RelayPool(List.of(relayUri), FakeRelay::accepting);
  }

  private static final byte[] KEY_MATERIAL =
      HexFormat.of().parseHex(Identity.generateRandomIdentity().getPrivateKey().toHexString());
}
