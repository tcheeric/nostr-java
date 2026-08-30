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
 * Changes what an identity is called.
 *
 * <p>Freely allowed: an alias is this server's private label for a key, not part of the account,
 * so renaming is invisible to the network and trivially undone by renaming back.
 */
public final class RenameIdentityTool implements NostrTool {

  private final IdentityLifecycle lifecycle;

  /**
   * @param lifecycle performs the keystore change
   */
  public RenameIdentityTool(@NonNull IdentityLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  @Override
  public String name() {
    return "nostr_rename_identity";
  }

  @Override
  public String description() {
    return "Rename one of this server's identities. The account itself is unchanged; only the"
        + " local label changes.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "alias", Map.of("type", "string", "description", "The identity's current name."),
            "newAlias", Map.of("type", "string", "description", "The name to use instead.")),
        "required",
        List.of("alias", "newAlias"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      ToolArguments arguments = new ToolArguments(request.arguments());
      String currentAlias = arguments.requireText("alias");
      IdentitySummary renamed = lifecycle.rename(currentAlias, arguments.requireText("newAlias"));
      return CallToolResult.builder()
          .structuredContent(Map.of("alias", renamed.alias(), "publicKey", renamed.publicKey()))
          .addTextContent("Renamed '" + currentAlias + "' to '" + renamed.alias() + "'.")
          .build();
    } catch (ToolException e) {
      return e.asResult();
    } catch (nostr.mcp.identity.IdentityUnknownException e) {
      return ToolFailure.IDENTITY_UNKNOWN.with(e.getMessage());
    }
  }
}
