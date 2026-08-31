package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.NostrIdentifier;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fetches a note together with the replies to it.
 *
 * <p>Two queries, because a thread is not something a relay can return in one: the note is found
 * by id, and its replies are found by their {@code e} tag pointing back at it. Doing that in a
 * tool rather than leaving it to the agent saves a round trip and, more importantly, saves the
 * model from having to know how NIP-10 threading is encoded.
 *
 * <p>Only direct replies are fetched. Following the tree to arbitrary depth multiplies queries
 * for diminishing returns, and a conversation an agent needs to summarise is nearly always the
 * note plus what people said back.
 */
public final class FetchThreadTool implements NostrTool {

  private static final int TEXT_NOTE_KIND = 1;
  private static final String REPLY_TAG = "e";

  private final EventQuery eventQuery;
  private final QueryLimits limits;

  /**
   * @param eventQuery runs both queries
   * @param limits the configured query bounds
   */
  public FetchThreadTool(@NonNull EventQuery eventQuery, @NonNull QueryLimits limits) {
    this.eventQuery = eventQuery;
    this.limits = limits;
  }

  @Override
  public String name() {
    return "nostr_fetch_thread";
  }

  @Override
  public String description() {
    return "Fetch a note and the replies to it, so a conversation can be read in order.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "eventId",
            Map.of("type", "string", "description", "The note, as hex, note or nevent.")),
        "required",
        List.of("eventId"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return fetch(
          NostrIdentifier.eventId(
                  "eventId", new ToolArguments(request.arguments()).requireText("eventId"))
              .hex());
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult fetch(String eventId) {
    Optional<GenericEvent> root =
        eventQuery
            .run(
                EventFilter.builder().id(eventId).limit(1).build(),
                1,
                limits.queryTimeout())
            .events()
            .stream()
            .findFirst();

    if (root.isEmpty()) {
      return CallToolResult.builder()
          .structuredContent(Map.of("eventId", eventId, "found", false))
          .addTextContent("No note with that id was found on the configured relays.")
          .build();
    }

    List<GenericEvent> replies = repliesTo(eventId);
    return CallToolResult.builder()
        .structuredContent(
            Map.of(
                "root", describe(root.get()),
                "replies", replies.stream().map(FetchThreadTool::describe).toList(),
                "replyCount", replies.size()))
        .addTextContent(
            replies.isEmpty()
                ? "Found the note; nobody has replied to it."
                : "Found the note and " + replies.size() + " repl" + (replies.size() == 1 ? "y." : "ies."))
        .build();
  }

  /**
   * Finds replies oldest first, since a conversation read newest first is hard to follow.
   */
  private List<GenericEvent> repliesTo(String eventId) {
    return eventQuery
        .run(
            EventFilter.builder()
                .kind(TEXT_NOTE_KIND)
                .addTagFilter(REPLY_TAG, eventId)
                .limit(limits.maxEventsPerQuery())
                .build(),
            limits.maxEventsPerQuery(),
            limits.queryTimeout())
        .events()
        .stream()
        .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
        .toList();
  }

  private static Map<String, Object> describe(GenericEvent event) {
    Map<String, Object> described = new LinkedHashMap<>();
    described.put("id", event.getId());
    described.put("pubkey", event.getPubKey() == null ? null : event.getPubKey().toHexString());
    described.put("created_at", event.getCreatedAt());
    described.put("content", event.getContent());
    return described;
  }
}
