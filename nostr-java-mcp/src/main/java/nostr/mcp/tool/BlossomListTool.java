package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.NostrIdentifier;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.blossom.BlobDescriptor;
import nostr.mcp.blossom.BlossomAuth;
import nostr.mcp.blossom.BlossomClient;
import nostr.mcp.blossom.BlossomServers;
import nostr.mcp.blossom.BlossomVerb;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.identity.SigningAlias;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lists the blobs a key has stored on a server.
 *
 * <p>Listing is a read, but BUD-12 still wants a signed token for it, so this tool signs without
 * going through the write guard. That is deliberate: a server configured {@code write-policy:
 * deny} must still be able to answer "what have I uploaded", and refusing to sign a token that
 * only reads would make a read-only server unable to read.
 */
public final class BlossomListTool implements NostrTool {

  private final BlossomServers servers;
  private final BlossomClient client;
  private final BlossomAuth auth;
  private final IdentityVault identityVault;

  /**
   * @param servers which server to list from
   * @param client how to ask it
   * @param auth builds the list token
   * @param identityVault signs the token and resolves whose blobs are meant
   */
  public BlossomListTool(
      @NonNull BlossomServers servers,
      @NonNull BlossomClient client,
      @NonNull BlossomAuth auth,
      @NonNull IdentityVault identityVault) {
    this.servers = servers;
    this.client = client;
    this.auth = auth;
    this.identityVault = identityVault;
  }

  @Override
  public String name() {
    return "nostr_blossom_list";
  }

  @Override
  public String description() {
    return "List the blobs a public key has stored on a Blossom server. Defaults to this"
        + " server's own identity.";
  }

  /**
   * The schema a host shows the model.
   *
   * <p>A bound server omits {@code identity}, and says nothing about one anywhere else either.
   * The whole point of binding is that there is no name to give, and an argument with exactly
   * one acceptable value is an invitation to pass a different one.
   */
  @Override
  public Map<String, Object> inputSchema() {
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put(
        "pubkey",
        Map.of(
            "type",
            "string",
            "description",
            "Whose blobs to list, as hex or npub. Omit for this server's own key."));
    properties.put(
        "server",
        Map.of(
            "type",
            "string",
            "description",
            BlobHash.describeServerArgument(servers.configured(), "list from")));
    if (!identityVault.binding().isBound()) {
      properties.put(
          "identity",
          Map.of(
              "type",
              "string",
              "description",
              "Alias whose key signs the list request. Omit to use the default."));
    }
    return Map.of("type", "object", "properties", properties, "required", List.of());
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return list(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult list(ToolArguments arguments) {
    String alias = SigningAlias.resolve(identityVault, arguments.text("identity"));
    String owner = resolveOwner(arguments, alias);
    String server = servers.resolve(arguments.text("server"));

    GenericEvent token = auth.tokenFor(BlossomVerb.LIST, Optional.empty());
    SigningAlias.sign(identityVault, alias, token);

    List<BlobDescriptor> blobs = client.list(server, owner, auth.headerValue(token));
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "server", server,
                "pubkey", owner,
                "blobs", blobs.stream().map(BlobDescriptor::asStructuredContent).toList(),
                "count", blobs.size()))
        .addTextContent(summarise(server, blobs))
        .build();
  }

  /**
   * Describes the listing, mentioning a total size only when the server gave one.
   *
   * <p>blossom-server reports {@code "size": 0} for every blob it lists, so summing them and
   * saying "0 bytes in total" would state something false about files that plainly exist.
   */
  private String summarise(String server, List<BlobDescriptor> blobs) {
    if (blobs.isEmpty()) {
      return "No blobs stored on " + server + " for that key.";
    }
    long bytes = blobs.stream().mapToLong(BlobDescriptor::size).sum();
    String count = blobs.size() + " blob" + (blobs.size() == 1 ? "" : "s") + " on " + server;
    return bytes > 0 ? count + ", " + bytes + " bytes in total." : count + ".";
  }

  /**
   * Whose blobs to list.
   *
   * <p>Falls back to the signing identity rather than refusing, since "what have I uploaded" is
   * the question this tool almost always answers and the key is already known.
   */
  private String resolveOwner(ToolArguments arguments, String alias) {
    return arguments
        .text("pubkey")
        .map(pubkey -> NostrIdentifier.publicKey("pubkey", pubkey).hex())
        .orElseGet(() -> identityVault.publicKeyOf(alias).toHexString());
  }
}
