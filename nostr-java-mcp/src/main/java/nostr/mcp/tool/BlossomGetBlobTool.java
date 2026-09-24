package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.blossom.BlobDescriptor;
import nostr.mcp.blossom.BlossomClient;
import nostr.mcp.blossom.BlossomServers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Finds which server holds a blob, and what it is.
 *
 * <p>Answers with a URL and the blob's size and type, never with its bytes. A tool result is
 * read into a model's context, and a megabyte of image would be both useless there and ruinous
 * to it; the URL is what a note needs anyway.
 *
 * <p>Asks each configured server in turn, because the same hash may be on any of them and a
 * blob missing from the first is the normal case rather than a failure.
 */
public final class BlossomGetBlobTool implements NostrTool {

  private final BlossomServers servers;
  private final BlossomClient client;

  /**
   * @param servers which servers to look on
   * @param client how to ask them
   */
  public BlossomGetBlobTool(@NonNull BlossomServers servers, @NonNull BlossomClient client) {
    this.servers = servers;
    this.client = client;
  }

  @Override
  public String name() {
    return "nostr_blossom_get";
  }

  @Override
  public String description() {
    return "Find a Blossom blob by its sha256 hash and return its URL, size and type. Returns a"
        + " link, never the file's contents.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "sha256",
                Map.of("type", "string", "description", "The blob's sha256 hash, as lowercase hex."),
            "server",
                Map.of(
                    "type",
                    "string",
                    "description",
                    servers.configured().isEmpty()
                        ? "The Blossom server to look on, as a full https:// URL. Required: this"
                            + " server has none configured."
                        : "The Blossom server to look on. Omit to try all configured: "
                            + servers.configured())),
        "required",
        List.of("sha256"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return find(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  /**
   * Asks each server in turn, and keeps asking when one cannot answer.
   *
   * <p>A server being down is not evidence about the blob. Letting an unreachable first server
   * end the search would report "could not reach server-one" while the blob sits on server-two,
   * which is the failure this tool exists to avoid: the whole point of hash addressing is that
   * the same blob may be on any of them.
   *
   * <p>Only when every server failed for a reason other than "not here" is that reported, since
   * "no server holds it" would then be a claim nobody checked.
   */
  private CallToolResult find(ToolArguments arguments) {
    String sha256 = BlobHash.require(arguments, "sha256");
    List<String> candidates = servers.resolveAll(arguments.text("server"));
    List<String> unreachable = new ArrayList<>();
    for (String server : candidates) {
      try {
        Optional<BlobDescriptor> found = client.head(server, sha256);
        if (found.isPresent()) {
          return describe(server, found.get());
        }
      } catch (ToolException unusable) {
        unreachable.add(server + " (" + unusable.getMessage() + ")");
      }
    }
    if (unreachable.size() == candidates.size()) {
      throw ToolFailure.BLOB_SERVER_UNREACHABLE.raise(
          "No server could be asked about " + sha256 + ": " + unreachable);
    }
    throw ToolFailure.BLOB_NOT_FOUND.raise(
        "No blob "
            + sha256
            + " on "
            + candidates
            + ". It may be on a server not listed here."
            + (unreachable.isEmpty() ? "" : " These could not be asked: " + unreachable + "."));
  }

  /**
   * Describes the size and type, when the server said anything about them.
   *
   * <p>BUD-01 asks a server to echo {@code Content-Length} and {@code Content-Type} on a HEAD,
   * and blossom-server sends neither. Saying "0 bytes" because a header was missing would be a
   * worse answer than not mentioning the size at all.
   */
  private String describeSize(BlobDescriptor blob) {
    if (blob.size() <= 0) {
      return "";
    }
    return " (" + blob.size() + " bytes, " + blob.type() + ")";
  }

  private CallToolResult describe(String server, BlobDescriptor blob) {
    Map<String, Object> structured = new LinkedHashMap<>(blob.asStructuredContent());
    structured.put("server", server);
    return CallToolResult.builder()
        .structuredContent(structured)
        .addTextContent("Found on " + server + ": " + blob.url() + describeSize(blob) + ".")
        .build();
  }
}
