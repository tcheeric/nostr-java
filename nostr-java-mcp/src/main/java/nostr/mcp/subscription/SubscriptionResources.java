package nostr.mcp.subscription;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;

import java.util.List;

/**
 * Exposes subscriptions as MCP resources so a host can watch them.
 *
 * <p>A host that supports resource subscriptions is notified when events arrive and can read
 * them without the model deciding to poll, which is the difference between "tell me when I am
 * mentioned" working and requiring the agent to remember to check. A host that does not support
 * them loses nothing, because the polling tool remains.
 *
 * <p>Reading a resource does <em>not</em> drain the buffer, unlike the tool. A notification may
 * reach several observers, and a read that consumed the events would mean whichever one arrived
 * first silently took them from the others.
 */
public final class SubscriptionResources {

  private static final String URI_PREFIX = "nostr://subscription/";
  private static final String MEDIA_TYPE = "application/json";

  private SubscriptionResources() {}

  /**
   * The resource URI naming one subscription.
   *
   * @param subscriptionId the subscription
   * @return its URI
   */
  public static String uriFor(@NonNull String subscriptionId) {
    return URI_PREFIX + subscriptionId;
  }

  /**
   * A resource template covering every subscription.
   *
   * @param registry where open subscriptions live
   * @return the resource specification to register with the server
   */
  public static SyncResourceSpecification specification(@NonNull SubscriptionRegistry registry) {
    Resource resource =
        Resource.builder()
            .uri(URI_PREFIX + "{subscriptionId}")
            .name("Nostr subscription")
            .description("Events buffered by an open subscription, updated as they arrive.")
            .mimeType(MEDIA_TYPE)
            .build();
    return new SyncResourceSpecification(
        resource, (exchange, request) -> read(registry, request.uri()));
  }

  private static ReadResourceResult read(SubscriptionRegistry registry, String uri) {
    String subscriptionId = uri.substring(uri.lastIndexOf('/') + 1);
    return registry
        .find(subscriptionId)
        .map(subscription -> new ReadResourceResult(List.of(contentsOf(uri, subscription))))
        .orElseGet(
            () ->
                new ReadResourceResult(
                    List.of(
                        new TextResourceContents(
                            uri, MEDIA_TYPE, "{\"error\":\"no such subscription\"}"))));
  }

  /**
   * Renders the buffer without consuming it, so several observers see the same events.
   */
  private static TextResourceContents contentsOf(String uri, LiveSubscription subscription) {
    StringBuilder json = new StringBuilder("{\"subscriptionId\":\"").append(subscription.id());
    json.append("\",\"waiting\":").append(subscription.depth());
    json.append(",\"droppedCount\":").append(subscription.droppedCount());
    json.append(",\"backlogDrained\":").append(subscription.backlogDrained());
    json.append('}');
    return new TextResourceContents(uri, MEDIA_TYPE, json.toString());
  }

  /**
   * How many events a subscription is holding, for a caller rendering its own summary.
   *
   * @param events the events to describe
   * @return the count
   */
  public static int countOf(@NonNull List<GenericEvent> events) {
    return events.size();
  }
}
