package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.EventFilterArguments;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.query.QueryResult;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Answers a question about what is on the relays.
 *
 * <p>The general read primitive: everything an agent wants to know about existing events is a
 * filter over authors, kinds, tags and time. More specific tools exist where the decoding is
 * non-obvious, such as profiles, but this is the one that makes the module useful for questions
 * nobody anticipated.
 */
public final class QueryEventsTool implements NostrTool {

  private final EventQuery eventQuery;
  private final QueryLimits limits;
  private final Clock clock;

  /**
   * @param eventQuery runs the query against the relays
   * @param limits the configured bounds on size and duration
   * @param clock what "now" means when resolving relative times
   */
  public QueryEventsTool(
      @NonNull EventQuery eventQuery, @NonNull QueryLimits limits, @NonNull Clock clock) {
    this.eventQuery = eventQuery;
    this.limits = limits;
    this.clock = clock;
  }

  @Override
  public String name() {
    return "nostr_query_events";
  }

  @Override
  public String description() {
    return "Query the relays for events matching a filter. Authors accept hex or npub; times"
        + " accept a relative age such as '24h', an ISO-8601 timestamp, or a date.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return EventFilterArguments.schemaWith(
        Map.of(
            "limit",
            Map.of(
                "type",
                "integer",
                "description",
                "Most events to return (capped at " + limits.maxEventsPerQuery() + ").")));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return answer(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult answer(ToolArguments arguments) {
    QueryResult result =
        eventQuery.run(filterFrom(arguments), requestedLimit(arguments), limits.queryTimeout());
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "events", result.events().stream().map(QueryEventsTool::describe).toList(),
                "count", result.events().size(),
                "truncated", result.truncated(),
                "timedOut", result.timedOut()))
        .addTextContent(summarise(result))
        .build();
  }

  /**
   * Asks the relay for one more event than will be returned.
   *
   * <p>A relay honours the filter's own limit, so asking for exactly the number wanted makes a
   * complete answer and a truncated one arrive identically: the stream simply ends. Requesting
   * one extra means an answer that overflows is visibly an overflow, and the extra event is
   * discarded rather than shown.
   */
  private EventFilter filterFrom(ToolArguments arguments) {
    return EventFilterArguments.toFilterBuilder(arguments, clock)
        .limit(requestedLimit(arguments) + 1)
        .build();
  }

  /**
   * Caps the caller's limit at the configured maximum.
   *
   * <p>Capped rather than refused: an agent asking for more than the deployment allows has made
   * no error, and failing the call would leave it with nothing where a smaller answer is useful.
   * The result says whether the cap actually bit.
   */
  private int requestedLimit(ToolArguments arguments) {
    return arguments
        .integer("limit")
        .filter(limit -> limit > 0)
        .map(limit -> Math.min(limit, limits.maxEventsPerQuery()))
        .orElseGet(limits::maxEventsPerQuery);
  }

  private static Map<String, Object> describe(GenericEvent event) {
    Map<String, Object> described = new LinkedHashMap<>();
    described.put("id", event.getId());
    described.put("pubkey", event.getPubKey() == null ? null : event.getPubKey().toHexString());
    described.put("kind", event.getKind());
    described.put("created_at", event.getCreatedAt());
    described.put("content", event.getContent());
    return described;
  }

  /**
   * States plainly when an answer is partial.
   *
   * <p>An agent that cannot tell "nothing matched" from "I stopped looking" will report the
   * first when the truth was the second, so both bounds are named in the text the model reads.
   */
  private String summarise(QueryResult result) {
    if (result.events().isEmpty()) {
      return result.timedOut()
          ? "No events arrived before the query timed out. The relays may still be replaying;"
              + " try again or narrow the filter."
          : "No events matched.";
    }
    StringBuilder summary = new StringBuilder();
    summary.append("Found ").append(result.events().size()).append(" event");
    if (result.events().size() != 1) {
      summary.append('s');
    }
    summary.append(" across ").append(result.relayCount()).append(" relay");
    if (result.relayCount() != 1) {
      summary.append('s');
    }
    summary.append('.');
    if (result.truncated()) {
      summary.append(" The limit was reached, so there may be more; narrow the filter to see the rest.");
    }
    if (result.timedOut()) {
      summary.append(" The relays had not finished replaying, so this may be incomplete.");
    }
    return summary.toString();
  }

}
