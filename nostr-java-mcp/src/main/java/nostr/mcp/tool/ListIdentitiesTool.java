package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.identity.IdentitySummary;
import nostr.mcp.identity.IdentityVault;

import java.util.List;
import java.util.Map;

/**
 * Reports which identities the server can sign with.
 *
 * <p>An agent needs this to name an identity at all, and it is the natural place for a key to
 * leak, so what it returns is constrained by type rather than by care: {@link IdentitySummary}
 * has no field that could hold one.
 */
public final class ListIdentitiesTool implements NostrTool {

  private final IdentityVault identityVault;

  /**
   * @param identityVault the keys this server holds
   */
  public ListIdentitiesTool(@NonNull IdentityVault identityVault) {
    this.identityVault = identityVault;
  }

  @Override
  public String name() {
    return "nostr_list_identities";
  }

  @Override
  public String description() {
    return "List the identities this server can sign with, by alias and public key.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of("type", "object", "properties", Map.of());
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    List<IdentitySummary> identities = identityVault.list();
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "identities", identities,
                "default", identityVault.defaultAlias().orElse("")))
        .addTextContent(summarise(identities))
        .build();
  }

  /**
   * States what an agent must do next when the answer is ambiguous.
   *
   * <p>Several identities with no default is the condition that makes signing fail, so saying so
   * here saves the agent discovering it by posting from the wrong account.
   */
  private String summarise(List<IdentitySummary> identities) {
    if (identities.isEmpty()) {
      return "No identities are configured; this server cannot sign.";
    }
    return identityVault
        .defaultAlias()
        .map(alias -> identities.size() + " identities, signing as '" + alias + "' by default")
        .orElse(
            identities.size()
                + " identities and no default; name one when signing or signing will fail");
  }
}
