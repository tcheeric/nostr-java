package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.subscription.LiveSubscription;
import nostr.mcp.subscription.SubscriptionRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports every open subscription and how healthy it is.
 *
 * <p>Needed because a subscription can degrade without failing: relays drop out, buffers
 * overflow, and the agent that opened it may be a different conversation from the one now
 * reading. This is how an agent discovers what it is already watching, and whether what it is
 * receiving is the whole picture.
 */
public final class ListSubscriptionsTool implements NostrTool {

  private final SubscriptionRegistry subscriptions;

  /**
   * @param subscriptions where open subscriptions live
   */
  public ListSubscriptionsTool(@NonNull SubscriptionRegistry subscriptions) {
    this.subscriptions = subscriptions;
  }

  @Override
  public String name() {
    return "nostr_list_subscriptions";
  }

  @Override
  public String description() {
    return "List the open subscriptions, how many events are waiting in each, and whether any"
        + " relays have dropped out.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of("type", "object", "properties", Map.of());
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    List<LiveSubscription> open = subscriptions.list();
    return CallToolResult.builder()
        .structuredContent(
            Map.of("subscriptions", open.stream().map(ListSubscriptionsTool::describe).toList()))
        .addTextContent(summarise(open))
        .build();
  }

  private static Map<String, Object> describe(LiveSubscription subscription) {
    Map<String, Object> described = new LinkedHashMap<>();
    described.put("subscriptionId", subscription.id());
    described.put("filter", subscription.filter().toString());
    described.put("waiting", subscription.depth());
    described.put("droppedCount", subscription.droppedCount());
    described.put("backlogDrained", subscription.backlogDrained());
    described.put("relays", List.copyOf(subscription.subscribedRelays()));
    described.put("failedRelays", subscription.failures());
    return described;
  }

  private String summarise(List<LiveSubscription> open) {
    if (open.isEmpty()) {
      return "No open subscriptions. Start one with nostr_subscribe.";
    }
    StringBuilder summary = new StringBuilder();
    open.forEach(
        subscription ->
            summary
                .append(subscription.id())
                .append(": ")
                .append(subscription.depth())
                .append(" waiting")
                .append(subscription.droppedCount() > 0 ? ", " + subscription.droppedCount() + " dropped" : "")
                .append(subscription.failures().isEmpty() ? "" : ", relays down: " + subscription.failures().keySet())
                .append('\n'));
    return summary.toString().strip();
  }
}
