package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.ConnectionState;
import nostr.mcp.relay.RelayDirectory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports the configured relays and whether each is currently carrying traffic.
 *
 * <p>This is the module's tracer bullet: it needs no keystore, writes nothing, and touches every
 * layer from transport to the SDK, so it proves the wiring end to end. It is also genuinely
 * useful, because "why did my note only reach two relays" is unanswerable without it.
 */
public final class ListRelaysTool implements NostrTool {

  private final RelayDirectory relayDirectory;
  private final RelayPool relayPool;

  /**
   * @param relayDirectory the logical relay names an agent can use
   * @param relayPool the live connections whose state is reported
   */
  public ListRelaysTool(@NonNull RelayDirectory relayDirectory, @NonNull RelayPool relayPool) {
    this.relayDirectory = relayDirectory;
    this.relayPool = relayPool;
  }

  @Override
  public String name() {
    return "nostr_list_relays";
  }

  @Override
  public String description() {
    return "List the configured Nostr relays and their current connection state.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of("type", "object", "properties", Map.of());
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    List<Map<String, Object>> relays = new ArrayList<>();
    for (String relayUri : relayDirectory.allRelayUris()) {
      relays.add(describe(relayUri));
    }
    return CallToolResult.builder()
        .structuredContent(Map.of("relays", relays, "names", relayDirectory.names()))
        .addTextContent(summarise(relays))
        .build();
  }

  private Map<String, Object> describe(String relayUri) {
    Map<String, Object> relay = new LinkedHashMap<>();
    relay.put("uri", relayUri);
    relay.put("state", stateOf(relayUri));
    return relay;
  }

  /**
   * The relay's state, or {@code UNREACHABLE} when the pool holds no connection for it.
   *
   * <p>A relay the pool never managed to connect to has no state of its own, and reporting it as
   * absent would hide the very thing an operator is asking about.
   */
  private String stateOf(String relayUri) {
    return relayPool
        .getConnectionState(relayUri)
        .map(ConnectionState::name)
        .orElse("UNREACHABLE");
  }

  private String summarise(List<Map<String, Object>> relays) {
    long connected =
        relays.stream().filter(relay -> ConnectionState.CONNECTED.name().equals(relay.get("state"))).count();
    return connected + " of " + relays.size() + " relays connected";
  }
}
