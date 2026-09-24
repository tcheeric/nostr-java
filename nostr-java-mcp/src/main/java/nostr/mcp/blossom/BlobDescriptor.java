package nostr.mcp.blossom;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What a Blossom server says about one stored blob.
 *
 * <p>BUD-02 fixes the five fields, and the {@code url} is the one that matters to a caller: it
 * is the link that can be put in a note. The hash is carried alongside so the caller can check
 * that the server stored what was sent rather than something it decided to re-encode.
 *
 * @param url where the blob can be fetched, extension included
 * @param sha256 the blob's hash, lowercase hex
 * @param size the blob's size in bytes
 * @param type the blob's MIME type
 * @param uploaded when the server stored it, as a unix timestamp
 */
public record BlobDescriptor(String url, String sha256, long size, String type, long uploaded) {

  private static final String FALLBACK_TYPE = "application/octet-stream";

  /**
   * Read a descriptor from a server's response.
   *
   * <p>Missing fields are tolerated rather than fatal. Servers add fields freely and some omit
   * the ones BUD-02 calls for; refusing a response whose {@code uploaded} is absent would fail an
   * upload that actually succeeded, which is the worst outcome available here.
   *
   * @param node the JSON object the server returned
   * @return the descriptor it describes
   */
  public static BlobDescriptor from(@NonNull JsonNode node) {
    return new BlobDescriptor(
        node.path("url").asText(""),
        node.path("sha256").asText(""),
        node.path("size").asLong(0),
        text(node, "type", FALLBACK_TYPE),
        node.path("uploaded").asLong(0));
  }

  /**
   * Render this descriptor for a tool result.
   *
   * @return the fields, in the order a reader wants them
   */
  public Map<String, Object> asStructuredContent() {
    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("url", url);
    structured.put("sha256", sha256);
    structured.put("size", size);
    structured.put("type", type);
    structured.put("uploaded", uploaded);
    return structured;
  }

  private static String text(JsonNode node, String field, String fallback) {
    String value = node.path(field).asText("");
    return value.isBlank() ? fallback : value;
  }
}
