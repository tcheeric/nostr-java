package nostr.api.client;

import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.message.ReqMessage;

/**
 * Coordinates REQ message dispatch across registered relay clients.
 */
public final class NostrRequestDispatcher {

  private final NostrRelayRegistry relayRegistry;

  public NostrRequestDispatcher(NostrRelayRegistry relayRegistry) {
    this.relayRegistry = relayRegistry;
  }

  public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    relayRegistry.ensureRequestClients(subscriptionId);
    return relayRegistry.requestHandlers(subscriptionId).stream()
        .map(handler -> handler.sendRequest(filters, handler.getRelayName()))
        .flatMap(List::stream)
        .toList();
  }

  public List<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
    return filtersList.stream()
        .map(filters -> sendRequest(filters, subscriptionId))
        .flatMap(List::stream)
        .distinct()
        .toList();
  }

  public static List<String> sendRequest(
      @NonNull SpringWebSocketClient client,
      @NonNull Filters filters,
      @NonNull String subscriptionId)
      throws IOException {
    return client.send(new ReqMessage(subscriptionId, filters));
  }
}
