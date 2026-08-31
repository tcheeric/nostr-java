package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityLifecycle;
import nostr.mcp.identity.IdentitySummary;
import nostr.mcp.identity.KeyImportSource;

import java.util.List;
import java.util.Map;

/**
 * Imports an existing key that the server reads for itself.
 *
 * <p>This tool takes no key material, and that is its defining property rather than an
 * inconvenience. An argument holding an {@code nsec} would put the user's key into the model's
 * context, the host's conversation log, and probably a third-party inference API. So {@code
 * source} names a location the server reads directly, and the model arranges an import it never
 * observes.
 */
public final class ImportIdentityTool implements NostrTool {

  private final IdentityLifecycle lifecycle;

  /**
   * @param lifecycle performs the keystore change
   */
  public ImportIdentityTool(@NonNull IdentityLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  @Override
  public String name() {
    return "nostr_import_identity";
  }

  @Override
  public String description() {
    return "Import an existing Nostr key that the server reads itself. Never send key material"
        + " to this tool; 'source' names where the server should read it from.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "alias", Map.of("type", "string", "description", "A short name for the identity."),
            "source",
                Map.of(
                    "type",
                    "string",
                    "description",
                    "Where the server reads the key from: 'file:/path/to/key',"
                        + " 'env:VARIABLE_NAME', or 'prompt'. Never the key itself.",
                    "examples", List.of("file:/home/user/key.txt", "env:NOSTR_IMPORT_KEY", "prompt")),
            "shredSource",
                Map.of(
                    "type",
                    "boolean",
                    "description",
                    "Overwrite and delete the key file after importing. Only applies to a file"
                        + " source.")),
        "required",
        List.of("alias", "source"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      ToolArguments arguments = new ToolArguments(request.arguments());
      String source = arguments.requireText("source");
      IdentitySummary imported = lifecycle.importFrom(arguments.requireText("alias"), source);
      boolean shredded = shredIfAsked(arguments, source);
      return CallToolResult.builder()
          .structuredContent(
              Map.of(
                  "alias", imported.alias(),
                  "publicKey", imported.publicKey(),
                  "npub", imported.npub(),
                  "sourceShredded", shredded))
          .addTextContent(describe(imported, shredded))
          .build();
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private boolean shredIfAsked(ToolArguments arguments, String source) {
    return arguments.text("shredSource").map(Boolean::parseBoolean).orElse(false)
        && KeyImportSource.shred(source);
  }

  private String describe(IdentitySummary imported, boolean shredded) {
    String summary =
        "Imported identity '" + imported.alias() + "' with public key " + imported.npub() + ".";
    return shredded ? summary + " The source file has been overwritten and deleted." : summary;
  }
}
