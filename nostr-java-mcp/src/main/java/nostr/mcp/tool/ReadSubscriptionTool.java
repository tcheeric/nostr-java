package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.subscription.LiveSubscription;
import nostr.mcp.subscription.SubscriptionRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects what a subscription has caught since it was last read.
 *
 * <p>Reading drains, so polling repeatedly yields only what is new. That is a command that also
 * answers, which is usually worth avoiding, but the alternative fills an agent's context with
 * the same events on every poll until nothing else fits.
 */
public final class ReadSubscriptionTool implements NostrTool {

  private final SubscriptionRegistry subscriptions;

  /**
   * @param subscriptions where open subscriptions live
   */
  public ReadSubscriptionTool(@NonNull SubscriptionRegistry subscriptions) {
    this.subscriptions = subscriptions;
  }

  @Override
  public String name() {
    return "nostr_read_subscription";
  }

  @Override
  public String description() {
    return "Collect the events a subscription has received since you last read it. Reading"
        + " empties it, so each event is returned once.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "subscriptionId",
            Map.of("type", "string", "description", "The id returned by nostr_subscribe.")),
        "required",
        List.of("subscriptionId"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      LiveSubscription subscription =
          subscriptions.require(new ToolArguments(request.arguments()).requireText("subscriptionId"));
      long droppedBefore = subscription.droppedCount();
      List<GenericEvent> events = subscription.drain();
      return CallToolResult.builder()
          .structuredContent(
              Map.of(
                  "subscriptionId", subscription.id(),
                  "events", events.stream().map(ReadSubscriptionTool::describe).toList(),
                  "count", events.size(),
                  "droppedCount", droppedBefore,
                  "backlogDrained", subscription.backlogDrained()))
          .addTextContent(summarise(subscription, events, droppedBefore))
          .build();
    } catch (ToolException e) {
      return e.asResult();
    }
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
   * Says what arrived, and warns about anything that did not.
   *
   * <p>Two things an agent cannot infer are stated outright: that the backlog is still replaying,
   * so an empty read is not yet an empty feed, and that events were dropped, so what it has is a
   * sample rather than the whole story.
   */
  private String summarise(
      LiveSubscription subscription, List<GenericEvent> events, long droppedCount) {
    StringBuilder summary = new StringBuilder();
    if (events.isEmpty()) {
      summary.append(
          subscription.backlogDrained()
              ? "Nothing new since the last read."
              : "Nothing yet: the relays are still replaying their stored events.");
    } else {
      summary.append("Received ").append(events.size()).append(events.size() == 1 ? " event." : " events.");
    }
    if (droppedCount > 0) {
      summary
          .append(" ")
          .append(droppedCount)
          .append(" event(s) were dropped because the buffer filled up, so this is not the whole")
          .append(" story; read more often or narrow the filter.");
    }
    if (!subscription.failures().isEmpty()) {
      summary.append(" Some relays have dropped out: ").append(subscription.failures().keySet()).append('.');
    }
    return summary.toString();
  }
}
