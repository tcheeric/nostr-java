package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityLifecycle;
import nostr.mcp.identity.IdentitySummary;

import java.util.List;
import java.util.Map;

/**
 * Creates a new identity, for "make me a throwaway account for this project".
 *
 * <p>Freely allowed, because creating a key is the one lifecycle operation that costs nothing to
 * undo: an unwanted identity is discarded by removing it, and until it publishes anything it
 * exists only on this machine.
 *
 * <p>The key is generated inside the vault and only the public half comes back, so an agent can
 * create an account it is able to use but unable to leak.
 */
public final class CreateIdentityTool implements NostrTool {

  private final IdentityLifecycle lifecycle;

  /**
   * @param lifecycle performs the keystore change
   */
  public CreateIdentityTool(@NonNull IdentityLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  @Override
  public String name() {
    return "nostr_create_identity";
  }

  @Override
  public String description() {
    return "Create a new Nostr identity in this server's keystore and return its public key."
        + " The private key stays on the server.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "alias",
            Map.of(
                "type",
                "string",
                "description",
                "A short name such as 'project-bot': lowercase letters, digits and hyphens.")),
        "required",
        List.of("alias"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      IdentitySummary created =
          lifecycle.create(new ToolArguments(request.arguments()).requireText("alias"));
      return CallToolResult.builder()
          .structuredContent(
              Map.of(
                  "alias", created.alias(),
                  "publicKey", created.publicKey(),
                  "npub", created.npub()))
          .addTextContent(
              "Created identity '" + created.alias() + "' with public key " + created.npub() + ".")
          .build();
    } catch (ToolException e) {
      return e.asResult();
    }
  }
}
