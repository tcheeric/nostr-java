package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.subscription.LiveSubscription;
import nostr.mcp.subscription.SubscriptionRegistry;

import java.util.List;
import java.util.Map;

/**
 * Stops watching, and frees what the subscription was holding.
 *
 * <p>Idle subscriptions are reaped eventually, but an agent that has finished with one should be
 * able to say so: the cap on open subscriptions is shared, so leaving them to time out spends a
 * limited resource for no reason.
 */
public final class UnsubscribeTool implements NostrTool {

  private final SubscriptionRegistry subscriptions;

  /**
   * @param subscriptions where open subscriptions live
   */
  public UnsubscribeTool(@NonNull SubscriptionRegistry subscriptions) {
    this.subscriptions = subscriptions;
  }

  @Override
  public String name() {
    return "nostr_unsubscribe";
  }

  @Override
  public String description() {
    return "Stop watching and discard anything the subscription had buffered.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "subscriptionId",
            Map.of("type", "string", "description", "The subscription to close.")),
        "required",
        List.of("subscriptionId"));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      String id = new ToolArguments(request.arguments()).requireText("subscriptionId");
      LiveSubscription closed = subscriptions.close(id);
      return CallToolResult.builder()
          .structuredContent(Map.of("subscriptionId", closed.id(), "closed", true))
          .addTextContent("Stopped watching '" + closed.id() + "'.")
          .build();
    } catch (ToolException e) {
      return e.asResult();
    }
  }
}
