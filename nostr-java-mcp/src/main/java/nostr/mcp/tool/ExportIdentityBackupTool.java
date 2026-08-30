package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityLifecycle;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes an encrypted backup and reports only where it is.
 *
 * <p>The result carries a path and never the contents, which is what makes a backup safe to take
 * from inside a conversation: the file is what protects the user against an accidental removal,
 * while the key itself stays off the model's context entirely.
 *
 * <p>A passphrase is required rather than optional. An unencrypted backup is a plaintext private
 * key sitting on disk, which converts a safety feature into the module's worst liability.
 */
public final class ExportIdentityBackupTool implements NostrTool {

  private final IdentityLifecycle lifecycle;

  /**
   * @param lifecycle writes the backup
   */
  public ExportIdentityBackupTool(@NonNull IdentityLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  @Override
  public String name() {
    return "nostr_export_identity_backup";
  }

  @Override
  public String description() {
    return "Write a passphrase-encrypted backup of an identity to a file on the server and"
        + " return its path. The key itself is never returned.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "alias", Map.of("type", "string", "description", "The identity to back up."),
            "path", Map.of("type", "string", "description", "Where the server should write the backup."),
            "passphrase",
                Map.of(
                    "type",
                    "string",
                    "description",
                    "Protects the backup file. Required, since an unencrypted backup is a"
                        + " plaintext private key on disk.")),
        "required",
        List.of("alias", "path", "passphrase"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      ToolArguments arguments = new ToolArguments(request.arguments());
      String alias = arguments.requireText("alias");
      Path written =
          lifecycle.exportBackup(
              alias,
              Path.of(arguments.requireText("path")),
              arguments.requireText("passphrase").toCharArray());
      return CallToolResult.builder()
          .structuredContent(Map.of("alias", alias, "path", written.toString()))
          .addTextContent(
              "Wrote an encrypted backup of '"
                  + alias
                  + "' to "
                  + written
                  + ". Keep the passphrase safe: without it the backup cannot be restored.")
          .build();
    } catch (ToolException e) {
      return e.asResult();
    } catch (nostr.mcp.identity.IdentityUnknownException e) {
      return ToolFailure.IDENTITY_UNKNOWN.with(e.getMessage());
    }
  }
}
