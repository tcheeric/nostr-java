package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.client.relay.FakeRelay;
import nostr.client.relay.RelayPool;
import nostr.mcp.relay.RelayDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies an agent can see which relays are configured and which are actually carrying traffic. */
class ListRelaysToolTest {

  private static final String FIRST_RELAY = "wss://relay.one";
  private static final String SECOND_RELAY = "wss://relay.two";
  private static final String DOWN_RELAY = "wss://relay.down";

  private final Map<String, FakeRelay> relays = new ConcurrentHashMap<>();

  // Verifies every configured relay is reported with its connection state, which is what makes
  // "why did my note only reach one relay" answerable.
  @Test
  void everyConfiguredRelayIsReportedWithItsState() throws Exception {
    try (RelayPool pool = poolOf(FIRST_RELAY, SECOND_RELAY)) {
      CallToolResult result = toolFor(pool, FIRST_RELAY, SECOND_RELAY).call(emptyRequest());

      List<Map<String, Object>> reported = reportedRelays(result);
      assertEquals(2, reported.size());
      assertEquals(FIRST_RELAY, reported.getFirst().get("uri"));
      assertEquals("CONNECTED", reported.getFirst().get("state"));
    }
  }

  // Verifies a relay that dropped is reported as such rather than omitted, since a silently
  // shorter list is how an operator fails to notice degradation.
  @Test
  void aRelayThatDroppedIsStillReported() throws Exception {
    try (RelayPool pool = poolOf(FIRST_RELAY, SECOND_RELAY)) {
      relays.get(SECOND_RELAY).dropConnection();

      List<Map<String, Object>> reported = reportedRelays(toolFor(pool, FIRST_RELAY, SECOND_RELAY).call(emptyRequest()));

      assertEquals(2, reported.size(), "the dropped relay disappeared from the report");
      assertEquals("CLOSED", reported.get(1).get("state"));
    }
  }

  // Verifies a relay the pool never reached is reported as unreachable rather than absent,
  // since its absence is exactly what an operator is asking about.
  @Test
  void aRelayThatWasNeverReachedIsReportedUnreachable() throws Exception {
    try (RelayPool pool =
        new RelayPool(
            List.of(FIRST_RELAY, DOWN_RELAY),
            relayUri -> {
              if (DOWN_RELAY.equals(relayUri)) {
                throw new IOException("connection refused");
              }
              return relays.computeIfAbsent(relayUri, FakeRelay::accepting);
            })) {

      List<Map<String, Object>> reported = reportedRelays(toolFor(pool, FIRST_RELAY, DOWN_RELAY).call(emptyRequest()));

      assertEquals("UNREACHABLE", reported.get(1).get("state"));
    }
  }

  // Verifies the human-readable summary states how many relays are carrying traffic, so an
  // agent can answer without parsing the structured payload.
  @Test
  void theSummaryStatesHowManyRelaysAreConnected() throws Exception {
    try (RelayPool pool = poolOf(FIRST_RELAY, SECOND_RELAY)) {
      relays.get(SECOND_RELAY).dropConnection();

      String summary = summaryOf(toolFor(pool, FIRST_RELAY, SECOND_RELAY).call(emptyRequest()));

      assertEquals("1 of 2 relays connected", summary);
    }
  }

  // Verifies the logical names an agent can use are reported, so it need not guess between
  // "read", "write" and a raw URI.
  @Test
  void theLogicalRelayNamesAreReported() throws Exception {
    try (RelayPool pool = poolOf(FIRST_RELAY)) {
      CallToolResult result = toolFor(pool, FIRST_RELAY).call(emptyRequest());

      @SuppressWarnings("unchecked")
      Iterable<String> names = (Iterable<String>) structured(result).get("names");
      assertTrue(names.iterator().hasNext());
    }
  }

  // Verifies the tool takes no arguments, since it reports state rather than answering a query.
  @Test
  void theToolTakesNoArguments() throws Exception {
    try (RelayPool pool = poolOf(FIRST_RELAY)) {
      ListRelaysTool tool = toolFor(pool, FIRST_RELAY);

      assertEquals("nostr_list_relays", tool.name());
      assertEquals(Map.of(), tool.inputSchema().get("properties"));
    }
  }

  private ListRelaysTool toolFor(RelayPool pool, String... relayUris) {
    return new ListRelaysTool(
        new RelayDirectory(Map.of(RelayDirectory.READ, List.of(relayUris))), pool);
  }

  private CallToolRequest emptyRequest() {
    return new CallToolRequest("nostr_list_relays", Map.of());
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> structured(CallToolResult result) {
    return (Map<String, Object>) result.structuredContent();
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> reportedRelays(CallToolResult result) {
    return (List<Map<String, Object>>) structured(result).get("relays");
  }

  private String summaryOf(CallToolResult result) {
    return ((TextContent) result.content().getFirst()).text();
  }

  private RelayPool poolOf(String... relayUris) {
    return new RelayPool(
        List.of(relayUris), relayUri -> relays.computeIfAbsent(relayUri, FakeRelay::accepting));
  }
}
