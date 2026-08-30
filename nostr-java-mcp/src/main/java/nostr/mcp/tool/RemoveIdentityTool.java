package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityLifecycle;
import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.identity.IdentitySummary;
import nostr.mcp.identity.IdentityVault;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Destroys an identity: the only genuinely irreversible thing in this module.
 *
 * <p>An npub with no nsec is a dead account. No relay, backup or protocol can restore it, and
 * every event ever signed with it is orphaned. So this tool is deliberately the hardest one to
 * use by accident: it confirms in two steps, and it refuses outright when no backup has been
 * taken unless the caller explicitly says the key is disposable.
 *
 * <p>That refusal is the important part. An agent acting on a vague instruction should not be
 * able to destroy an account on a hunch, and requiring it to state that no backup is needed
 * turns an ambiguous request into an explicit one.
 */
public final class RemoveIdentityTool implements NostrTool {

  private static final int TOKEN_BYTES = 16;

  private final Map<String, String> aliasByToken = new ConcurrentHashMap<>();
  private final SecureRandom tokens = new SecureRandom();
  private final IdentityLifecycle lifecycle;
  private final IdentityVault identityVault;
  private final IdentityPolicy policy;

  /**
   * @param lifecycle performs the removal
   * @param identityVault the identities this server holds
   * @param policy whether removal must be confirmed
   */
  public RemoveIdentityTool(
      @NonNull IdentityLifecycle lifecycle,
      @NonNull IdentityVault identityVault,
      @NonNull IdentityPolicy policy) {
    this.lifecycle = lifecycle;
    this.identityVault = identityVault;
    this.policy = policy;
  }

  @Override
  public String name() {
    return "nostr_remove_identity";
  }

  @Override
  public String description() {
    return "Permanently delete an identity's private key from this server. This cannot be undone"
        + " and the account can never be used again. Export a backup first.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "alias", Map.of("type", "string", "description", "The identity to delete."),
            "confirmationToken",
                Map.of(
                    "type",
                    "string",
                    "description",
                    "Token from the first call. Omit to preview; supply it to delete."),
            "acknowledgeNoBackup",
                Map.of(
                    "type",
                    "boolean",
                    "description",
                    "Set only when the user has explicitly said this key is disposable and needs"
                        + " no backup.")),
        "required",
        List.of("alias"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return removeOrPreview(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    } catch (nostr.mcp.identity.IdentityUnknownException e) {
      return ToolFailure.IDENTITY_UNKNOWN.with(e.getMessage());
    }
  }

  private CallToolResult removeOrPreview(ToolArguments arguments) {
    Optional<String> token = arguments.text("confirmationToken");
    if (token.isPresent()) {
      return remove(confirmedAlias(token.get()));
    }
    String alias = arguments.requireText("alias");
    IdentitySummary identity = require(alias);
    refuseWithoutBackupOrAcknowledgement(alias, arguments);
    return policy.requiresConfirmation() ? preview(identity) : remove(alias);
  }

  /**
   * Refuses to destroy a key nobody has backed up.
   *
   * <p>The acknowledgement cannot be inferred, only stated. An agent that was told "clean up the
   * test accounts" has no basis for deciding a key is disposable, so the tool makes it say so and
   * puts that claim in the conversation where a user can contradict it.
   */
  private void refuseWithoutBackupOrAcknowledgement(String alias, ToolArguments arguments) {
    boolean acknowledged =
        arguments.text("acknowledgeNoBackup").map(Boolean::parseBoolean).orElse(false);
    if (!lifecycle.hasBackup(alias) && !acknowledged) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "No backup of '"
              + alias
              + "' has been exported in this session, and deleting it destroys the account"
              + " permanently. Export one with nostr_export_identity_backup first, or set"
              + " acknowledgeNoBackup only if the user has said this key is disposable.");
    }
  }

  private String confirmedAlias(String token) {
    String alias = aliasByToken.remove(token);
    if (alias == null) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "That confirmation token is not valid. It may already have been used, or the server may"
              + " have restarted. Call this tool again without a token to start over.");
    }
    return alias;
  }

  private IdentitySummary require(String alias) {
    return identityVault
        .find(alias)
        .orElseThrow(
            () ->
                ToolFailure.IDENTITY_UNKNOWN.raise(
                    "No identity called '"
                        + alias
                        + "'. Available: "
                        + identityVault.list().stream().map(IdentitySummary::alias).toList()));
  }

  private CallToolResult preview(IdentitySummary identity) {
    String token = newToken();
    aliasByToken.put(token, identity.alias());
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "status", "awaiting-confirmation",
                "alias", identity.alias(),
                "publicKey", identity.publicKey(),
                "npub", identity.npub(),
                "hasBackup", lifecycle.hasBackup(identity.alias()),
                "confirmationToken", token))
        .addTextContent(
            "Nothing has been deleted yet. This would permanently destroy the private key for '"
                + identity.alias()
                + "' ("
                + identity.npub()
                + "), and the account could never be used again."
                + (lifecycle.hasBackup(identity.alias())
                    ? " A backup was exported earlier in this session."
                    : " No backup has been exported in this session.")
                + " To go ahead, call this tool again with confirmationToken='"
                + token
                + "'.")
        .build();
  }

  private CallToolResult remove(String alias) {
    IdentitySummary identity = require(alias);
    lifecycle.remove(alias);
    return CallToolResult.builder()
        .structuredContent(Map.of("alias", alias, "publicKey", identity.publicKey(), "removed", true))
        .addTextContent(
            "Deleted identity '" + alias + "' (" + identity.npub() + "). This cannot be undone.")
        .build();
  }

  private String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    tokens.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }
}
