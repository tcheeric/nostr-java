package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.blossom.BlobDescriptor;
import nostr.mcp.blossom.BlobSource;
import nostr.mcp.blossom.BlossomAuth;
import nostr.mcp.blossom.BlossomClient;
import nostr.mcp.blossom.BlossomServers;
import nostr.mcp.blossom.BlossomVerb;
import nostr.mcp.write.WriteGuard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copies media onto a Blossom server and hands back the URL.
 *
 * <p>This is the tool the module exists for: an agent has a link to media and needs it hosted
 * somewhere the user controls, addressed by hash, ready to put in a note.
 *
 * <p>It takes a URL rather than a file because this server has no business reading the disk it
 * runs on. An upload tool that accepted a path would let an agent put any readable file on a
 * public CDN, and no amount of care in the prompt makes that a good capability to hand out.
 * What the URL costs is that the media has to be reachable already; what it buys is that the
 * worst case is a public file being copied to a public server.
 *
 * <p>Registered only where writing is allowed, and signed through the write guard, so an upload
 * counts against the same rate limit as a published note. Uploading is publishing.
 */
public final class BlossomUploadTool implements NostrTool {

  private final BlossomServers servers;
  private final BlossomClient client;
  private final BlobSource blobSource;
  private final BlossomAuth auth;
  private final WriteGuard writeGuard;

  /**
   * @param servers where the blob goes
   * @param client how to put it there
   * @param blobSource fetches the media, under guard
   * @param auth builds the upload token
   * @param writeGuard applies the write policy and rate limit, and signs
   */
  public BlossomUploadTool(
      @NonNull BlossomServers servers,
      @NonNull BlossomClient client,
      @NonNull BlobSource blobSource,
      @NonNull BlossomAuth auth,
      @NonNull WriteGuard writeGuard) {
    this.servers = servers;
    this.client = client;
    this.blobSource = blobSource;
    this.auth = auth;
    this.writeGuard = writeGuard;
  }

  @Override
  public String name() {
    return "nostr_blossom_upload";
  }

  @Override
  public String description() {
    return "Upload media to a Blossom server from a URL and return the hosted URL. The media must"
        + " already be reachable over http(s); this cannot upload local files.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put(
        "sourceUrl",
        Map.of(
            "type",
            "string",
            "description",
            "The http(s) URL the media is currently at. It is fetched and re-hosted."));
    properties.put(
        "server",
        Map.of(
            "type",
            "string",
            "description",
            BlobHash.describeServerArgument(servers.configured(), "upload to")));
    if (!writeGuard.bindsOneIdentity()) {
      properties.put(
          "identity",
          Map.of("type", "string", "description", "Alias to upload as. Omit to use the default."));
    }
    return Map.of("type", "object", "properties", properties, "required", List.of("sourceUrl"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return upload(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult upload(ToolArguments arguments) {
    String sourceUrl = arguments.requireText("sourceUrl");
    String server = servers.resolve(arguments.text("server"));

    // Settle permission before fetching, not after. The token cannot be built until the blob is
    // hashed, but a caller who is not allowed to upload should not be able to make this server
    // pull a 16 MiB file into memory first.
    String alias = writeGuard.authorizeWrite(arguments.text("identity"));

    BlobSource.Blob blob = blobSource.fetch(sourceUrl);

    GenericEvent token = auth.tokenFor(BlossomVerb.UPLOAD, Optional.of(blob.sha256()));
    writeGuard.signAs(alias, token);

    BlobDescriptor stored =
        client.upload(
            server,
            blob.bytes(),
            blob.contentTypeOrDefault(),
            blob.sha256(),
            auth.headerValue(token));

    return report(server, alias, blob, stored);
  }

  /**
   * The blob's size, preferring what we know over what the server said.
   *
   * <p>Not every server fills this in: blossom-server answers uploads with {@code "size": 0}
   * whatever it stored. Passing that on would tell an agent a file it just uploaded is empty,
   * and the byte count we sent is a fact we actually have. The server's figure wins when it
   * gave one, since only it knows what it wrote.
   */
  private long reportedSize(BlobSource.Blob blob, BlobDescriptor stored) {
    return stored.size() > 0 ? stored.size() : blob.size();
  }

  /**
   * Reports what was stored, and says so plainly if the server changed it.
   *
   * <p>BUD-02 forbids a server from modifying a blob, so a hash that comes back different means
   * the URL does not point at what was sent. Reporting it rather than failing is the honest
   * option: the upload did happen, and the caller needs the URL that actually works.
   */
  private CallToolResult report(
      String server, String alias, BlobSource.Blob blob, BlobDescriptor stored) {
    boolean hashMatches = blob.sha256().equalsIgnoreCase(stored.sha256()) || stored.sha256().isBlank();
    Map<String, Object> structured = new LinkedHashMap<>(stored.asStructuredContent());
    structured.put("size", reportedSize(blob, stored));
    structured.put("server", server);
    structured.put("identity", alias);
    structured.put("hashMatches", hashMatches);

    StringBuilder summary =
        new StringBuilder("Uploaded to ")
            .append(server)
            .append(" as '")
            .append(alias)
            .append("': ")
            .append(stored.url());
    if (!hashMatches) {
      summary
          .append(". The server stored a different hash (")
          .append(stored.sha256())
          .append(") than was sent (")
          .append(blob.sha256())
          .append("), so it altered the file. Use the URL above, not the hash that was sent");
    }
    return CallToolResult.builder()
        .structuredContent(structured)
        .addTextContent(summary.append('.').toString())
        .build();
  }
}
