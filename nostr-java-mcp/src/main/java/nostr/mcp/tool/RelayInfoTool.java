package nostr.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.directory.WellKnownJson;
import nostr.mcp.relay.RelayDirectory;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports what a relay says about itself.
 *
 * <p>A relay's NIP-11 document states its limits, its policies and what it charges, which is how
 * an agent can answer "why was my note rejected" or "can I post something this long here" before
 * trying. Without it the only way to learn a relay's rules is to break one.
 */
public final class RelayInfoTool implements NostrTool {

  private static final String NIP11_MEDIA_TYPE = "application/nostr+json";

  private final RelayDirectory relayDirectory;
  private final WellKnownJson wellKnownJson;

  /**
   * @param relayDirectory resolves a configured relay name to its URI
   * @param wellKnownJson fetches the document
   */
  public RelayInfoTool(@NonNull RelayDirectory relayDirectory, @NonNull WellKnownJson wellKnownJson) {
    this.relayDirectory = relayDirectory;
    this.wellKnownJson = wellKnownJson;
  }

  @Override
  public String name() {
    return "nostr_relay_info";
  }

  @Override
  public String description() {
    return "Read a relay's NIP-11 document: its name, policies, limits and supported NIPs."
        + " Accepts a configured relay name or a wss:// URI.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "relay",
            Map.of(
                "type",
                "string",
                "description",
                "A configured relay name or a wss:// URI. Known names: " + relayDirectory.names())),
        "required",
        List.of("relay"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return describe(new ToolArguments(request.arguments()).requireText("relay"));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult describe(String relay) {
    String relayUri = resolve(relay);
    JsonNode document = wellKnownJson.fetch(httpUriOf(relayUri), NIP11_MEDIA_TYPE, relayUri);
    return CallToolResult.builder()
        .structuredContent(structuredFrom(relayUri, document))
        .addTextContent(summarise(relayUri, document))
        .build();
  }

  /**
   * Accepts either a configured name or a literal URI.
   *
   * <p>An agent that has just called {@code nostr_list_relays} holds names, while one repeating
   * something a user said holds a URI. Refusing either would make the tool awkward to reach.
   */
  private String resolve(String relay) {
    List<String> resolved = relayDirectory.resolve(List.of(relay));
    String candidate = resolved.isEmpty() ? relay : resolved.getFirst();
    refuseUnlessAddressable(relay, candidate);
    return candidate;
  }

  /**
   * Refuses something that is neither a known name nor a usable relay address.
   *
   * <p>The directory passes an unrecognised string through as though it were a URI, since a
   * caller may legitimately name a relay that was never configured. That leaves this tool as the
   * place where a value that is neither is caught: without the check a typo reaches the HTTP
   * client and surfaces as an {@code IllegalArgumentException} about an undefined scheme, which
   * tells the agent nothing it can act on.
   */
  private void refuseUnlessAddressable(String relay, String candidate) {
    if (candidate.startsWith("ws://") || candidate.startsWith("wss://")) {
      return;
    }
    throw ToolFailure.INVALID_ARGUMENT.raise(
        "'"
            + relay
            + "' is not a configured relay name, and is not a relay URI either; a relay URI"
            + " starts with wss:// or ws://. Known names: "
            + relayDirectory.names());
  }

  /**
   * Converts the websocket URI to the HTTPS one the document is served from.
   *
   * <p>NIP-11 publishes the document at the same host and path as the websocket endpoint, over
   * HTTP rather than the websocket scheme.
   */
  private URI httpUriOf(String relayUri) {
    return URI.create(relayUri.replaceFirst("^ws", "http"));
  }

  private Map<String, Object> structuredFrom(String relayUri, JsonNode document) {
    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("relay", relayUri);
    document.properties().forEach(entry -> structured.put(entry.getKey(), plainValue(entry.getValue())));
    return structured;
  }

  private Object plainValue(JsonNode value) {
    return value.isTextual() ? value.asText() : value.toString();
  }

  private String summarise(String relayUri, JsonNode document) {
    StringBuilder summary = new StringBuilder(document.path("name").asText(relayUri));
    String description = document.path("description").asText("");
    if (!description.isBlank()) {
      summary.append(": ").append(description);
    }
    JsonNode supportedNips = document.path("supported_nips");
    if (supportedNips.isArray() && !supportedNips.isEmpty()) {
      summary.append(" Supports NIPs ").append(supportedNips);
    }
    return summary.toString();
  }
}
