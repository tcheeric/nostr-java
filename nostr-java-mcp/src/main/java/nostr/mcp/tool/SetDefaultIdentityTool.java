package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityVault;

import java.util.List;
import java.util.Map;

/**
 * Chooses which identity signs when a caller names none.
 *
 * <p>Freely allowed and worth having, because the alternative to a default is that every write
 * carries an alias and an agent that forgets one is told the choice is ambiguous. Setting a
 * default resolves that permanently, and it changes nothing on the network.
 */
public final class SetDefaultIdentityTool implements NostrTool {

  private final IdentityVault identityVault;

  /**
   * @param identityVault the identities this server holds
   */
  public SetDefaultIdentityTool(@NonNull IdentityVault identityVault) {
    this.identityVault = identityVault;
  }

  @Override
  public String name() {
    return "nostr_set_default_identity";
  }

  @Override
  public String description() {
    return "Choose which identity this server signs with when no identity is named.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of("alias", Map.of("type", "string", "description", "The identity to sign as by default.")),
        "required",
        List.of("alias"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      String alias = new ToolArguments(request.arguments()).requireText("alias");
      identityVault.setDefault(alias);
      return CallToolResult.builder()
          .structuredContent(Map.of("default", alias))
          .addTextContent("This server will now sign as '" + alias + "' unless told otherwise.")
          .build();
    } catch (ToolException e) {
      return e.asResult();
    } catch (nostr.mcp.identity.IdentityUnknownException e) {
      return ToolFailure.IDENTITY_UNKNOWN.with(e.getMessage());
    }
  }
}
