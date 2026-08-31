package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.EventFilterArguments;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.subscription.LiveSubscription;
import nostr.mcp.subscription.SubscriptionRegistry;

import java.time.Clock;
import java.util.List;
import java.util.Map;

/**
 * Opens a standing watch for events that have not happened yet.
 *
 * <p>A query answers what a relay already holds; this answers "tell me when someone mentions
 * me", which no single call can. The subscription outlives the call and the agent collects from
 * it later.
 *
 * <p>It deliberately does not wait for the backlog. The SDK's subscribe returns before any
 * stored event arrives, and blocking until it drained would stall on any relay that never
 * answers, which is exactly what the SDK's own timeout exists to avoid. So the result says
 * plainly that history is still replaying.
 */
public final class SubscribeTool implements NostrTool {

  private final SubscriptionRegistry subscriptions;
  private final Clock clock;

  /**
   * @param subscriptions where open subscriptions live
   * @param clock what "now" means when resolving relative times
   */
  public SubscribeTool(@NonNull SubscriptionRegistry subscriptions, @NonNull Clock clock) {
    this.subscriptions = subscriptions;
    this.clock = clock;
  }

  @Override
  public String name() {
    return "nostr_subscribe";
  }

  @Override
  public String description() {
    return "Watch for events matching a filter as they arrive. Returns a subscription id; read"
        + " from it with nostr_read_subscription and close it with nostr_unsubscribe.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return EventFilterArguments.schema();
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      LiveSubscription opened =
          subscriptions.open(
              EventFilterArguments.toFilter(new ToolArguments(request.arguments()), clock));
      return CallToolResult.builder()
          .structuredContent(
              Map.of(
                  "subscriptionId", opened.id(),
                  "backlogDrained", opened.backlogDrained(),
                  "relays", List.copyOf(opened.subscribedRelays())))
          .addTextContent(
              "Watching as '"
                  + opened.id()
                  + "' across "
                  + opened.subscribedRelays().size()
                  + " relay(s). The relays are still replaying their stored events, so read it"
                  + " with nostr_read_subscription in a moment rather than immediately.")
          .build();
    } catch (ToolException e) {
      return e.asResult();
    }
  }
}
