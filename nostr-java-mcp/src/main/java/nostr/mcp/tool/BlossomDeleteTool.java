package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.blossom.BlossomAuth;
import nostr.mcp.blossom.BlossomClient;
import nostr.mcp.blossom.BlossomServers;
import nostr.mcp.blossom.BlossomVerb;
import nostr.mcp.write.WriteGuard;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Removes a blob from a Blossom server.
 *
 * <p>The only Blossom tool that destroys something, so under a confirming policy it previews
 * first and acts only when handed back the token, exactly as a publishing tool does. The two-step
 * is what turns a hallucinated deletion into a no-op: an agent that invented the intention will
 * not follow through with the token issued for it.
 *
 * <p>It keeps its own pending map rather than reusing {@link WriteGuard}'s, which holds a signed
 * event waiting for a relay. Generalising that to hold an arbitrary pending action would be a
 * larger change than the twenty lines it would save, and this way the guard stays about
 * publishing.
 *
 * <p>Worth knowing: deleting from one server removes nothing from any other. A blob is addressed
 * by its hash and may have been mirrored anywhere, so this is not a way to unpublish something.
 */
public final class BlossomDeleteTool implements NostrTool {

  private static final String CONFIRMATION_ARGUMENT = "confirmationToken";
  private static final int TOKEN_BYTES = 16;

  private final Map<String, PendingDelete> pendingByToken = new ConcurrentHashMap<>();
  private final SecureRandom tokens = new SecureRandom();
  private final BlossomServers servers;
  private final BlossomClient client;
  private final BlossomAuth auth;
  private final WriteGuard writeGuard;

  /**
   * @param servers which server to delete from
   * @param client how to ask it
   * @param auth builds the delete token
   * @param writeGuard applies the write policy and rate limit, and signs
   */
  public BlossomDeleteTool(
      @NonNull BlossomServers servers,
      @NonNull BlossomClient client,
      @NonNull BlossomAuth auth,
      @NonNull WriteGuard writeGuard) {
    this.servers = servers;
    this.client = client;
    this.auth = auth;
    this.writeGuard = writeGuard;
  }

  @Override
  public String name() {
    return "nostr_blossom_delete";
  }

  @Override
  public String description() {
    return "Delete a blob from a Blossom server by its sha256 hash. Removes it from that server"
        + " only; copies on other servers are unaffected.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put(
        "sha256", Map.of("type", "string", "description", "The blob's sha256 hash, as lowercase hex."));
    properties.put(
        "server",
        Map.of(
            "type",
            "string",
            "description",
            BlobHash.describeServerArgument(servers.configured(), "delete from")));
    if (!writeGuard.bindsOneIdentity()) {
      properties.put(
          "identity",
          Map.of("type", "string", "description", "Alias to delete as. Omit to use the default."));
    }
    if (writeGuard.requiresConfirmation()) {
      properties.put(
          CONFIRMATION_ARGUMENT,
          Map.of(
              "type",
              "string",
              "description",
              "Token from a previous preview. Omit to preview; supply it to delete."));
    }
    return Map.of("type", "object", "properties", properties, "required", List.of());
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return deleteOrPreview(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  /**
   * Runs whichever half of the conversation the caller is in.
   *
   * <p>The token decides, and when one is present the arguments are not read again: re-reading
   * them would let the hash change between what was shown and what is deleted.
   */
  private CallToolResult deleteOrPreview(ToolArguments arguments) {
    Optional<String> confirmation = arguments.text(CONFIRMATION_ARGUMENT);
    if (confirmation.isPresent()) {
      return deleteConfirmed(confirmation.get());
    }
    String sha256 = BlobHash.require(arguments, "sha256");
    String server = servers.resolve(arguments.text("server"));

    String alias = writeGuard.authorizeWrite(arguments.text("identity"));
    PendingDelete pending = new PendingDelete(sha256, server, alias);

    if (!writeGuard.requiresConfirmation()) {
      return performed(pending);
    }
    String confirmationToken = newToken();
    pendingByToken.put(confirmationToken, pending);
    return preview(confirmationToken, pending);
  }

  private CallToolResult deleteConfirmed(String confirmationToken) {
    PendingDelete pending = pendingByToken.remove(confirmationToken);
    if (pending == null) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "That confirmation token is not valid. It may already have been used, or the server may"
              + " have restarted. Call this tool again without a token to get a fresh preview.");
    }
    return performed(pending);
  }

  /**
   * Signs the token and sends the request, in that order and at this moment.
   *
   * <p>The token is built here rather than at preview time because a BUD-11 token expires. The
   * confirming policy exists so a person can be asked, and a person takes longer than a token
   * lives; a header signed at preview would be stale by the time the answer came back, and the
   * server would answer 401 with nothing to suggest that waiting was the problem.
   *
   * <p>Signing again costs no further quota. The write was authorized when it was previewed.
   */
  private CallToolResult performed(PendingDelete pending) {
    GenericEvent token = auth.tokenFor(BlossomVerb.DELETE, Optional.of(pending.sha256()));
    writeGuard.signAs(pending.alias(), token);
    client.delete(pending.server(), pending.sha256(), auth.headerValue(token));
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "sha256", pending.sha256(),
                "server", pending.server(),
                "identity", pending.alias(),
                "deleted", true))
        .addTextContent("Deleted " + pending.sha256() + " from " + pending.server() + ".")
        .build();
  }

  private CallToolResult preview(String confirmationToken, PendingDelete pending) {
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "status", "awaiting-confirmation",
                CONFIRMATION_ARGUMENT, confirmationToken,
                "sha256", pending.sha256(),
                "server", pending.server(),
                "identity", pending.alias()))
        .addTextContent(
            "Nothing has been deleted yet. This would remove "
                + pending.sha256()
                + " from "
                + pending.server()
                + " as '"
                + pending.alias()
                + "'. Copies on other servers are unaffected. To go ahead, call this tool again"
                + " with "
                + CONFIRMATION_ARGUMENT
                + "='"
                + confirmationToken
                + "'.")
        .build();
  }

  private String newToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    tokens.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }

  /**
   * A deletion that has been authorized and is waiting to be confirmed.
   *
   * <p>Holds what was described and not the token that will carry it out, because the token has
   * a deadline and the description does not.
   *
   * @param sha256 the blob to remove
   * @param server where to remove it from
   * @param alias the identity authorizing it
   */
  private record PendingDelete(String sha256, String server, String alias) {}
}
