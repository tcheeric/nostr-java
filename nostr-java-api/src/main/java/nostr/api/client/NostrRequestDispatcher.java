package nostr.api.client;

import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import nostr.base.SubscriptionId;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.message.ReqMessage;

/**
 * Coordinates REQ message dispatch across registered relay clients.
 */
public final class NostrRequestDispatcher {

  private final NostrRelayRegistry relayRegistry;

  /**
   * Create a dispatcher that leverages the registry to route REQ commands.
   *
   * @param relayRegistry registry that owns relay handlers
   */
  public NostrRequestDispatcher(NostrRelayRegistry relayRegistry) {
    this.relayRegistry = relayRegistry;
  }

  /**
   * Send a REQ message using the provided filters across all registered relays.
   *
   * @param filters filters describing the subscription
   * @param subscriptionId subscription identifier applied to handlers
   * @return list of relay responses
   */
  public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    return sendRequest(filters, SubscriptionId.of(subscriptionId));
  }

  public List<String> sendRequest(@NonNull Filters filters, @NonNull SubscriptionId subscriptionId) {
    relayRegistry.ensureRequestClients(subscriptionId);
    return relayRegistry.requestHandlers(subscriptionId).stream()
        .map(handler -> handler.sendRequest(filters, subscriptionId))
        .flatMap(List::stream)
        .toList();
  }

  /**
   * Send REQ messages for multiple filter sets under the same subscription identifier.
   *
   * @param filtersList list of filter definitions to send
   * @param subscriptionId subscription identifier applied to handlers
   * @return distinct collection of relay responses
   */
  public List<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
    SubscriptionId id = SubscriptionId.of(subscriptionId);
    return filtersList.stream()
        .map(filters -> sendRequest(filters, id))
        .flatMap(List::stream)
        .distinct()
        .toList();
  }

  /**
   * Convenience helper for issuing a REQ message via a specific client instance.
   *
   * @param client relay client used to send the REQ
   * @param filters filters describing the subscription
   * @param subscriptionId subscription identifier applied to the message
   * @return list of responses returned by the relay
   * @throws IOException if sending fails
   */
  public static List<String> sendRequest(
      @NonNull SpringWebSocketClient client,
      @NonNull Filters filters,
      @NonNull String subscriptionId)
      throws IOException {
    return client.send(new ReqMessage(subscriptionId, filters));
  }
}
