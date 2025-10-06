package nostr.api.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import nostr.api.WebSocketClientHandler;

/**
 * Manages the lifecycle of {@link WebSocketClientHandler} instances keyed by relay name.
 */
public final class NostrRelayRegistry {

  private final Map<String, WebSocketClientHandler> clientMap = new ConcurrentHashMap<>();
  private final WebSocketClientHandlerFactory factory;

  public NostrRelayRegistry(WebSocketClientHandlerFactory factory) {
    this.factory = factory;
  }

  public Map<String, WebSocketClientHandler> getClientMap() {
    return clientMap;
  }

  public void registerRelays(Map<String, String> relays) {
    for (Entry<String, String> relayEntry : relays.entrySet()) {
      clientMap.computeIfAbsent(
          relayEntry.getKey(),
          key -> createHandler(relayEntry.getKey(), relayEntry.getValue()));
    }
  }

  public Map<String, String> snapshotRelays() {
    return clientMap.values().stream()
        .collect(
            Collectors.toMap(
                WebSocketClientHandler::getRelayName,
                WebSocketClientHandler::getRelayUri,
                (prev, next) -> next,
                HashMap::new));
  }

  public List<WebSocketClientHandler> baseHandlers() {
    return clientMap.entrySet().stream()
        .filter(entry -> !entry.getKey().contains(":"))
        .map(Entry::getValue)
        .toList();
  }

  public List<WebSocketClientHandler> requestHandlers(String subscriptionId) {
    return clientMap.entrySet().stream()
        .filter(entry -> entry.getKey().endsWith(":" + subscriptionId))
        .map(Entry::getValue)
        .toList();
  }

  public void ensureRequestClients(String subscriptionId) {
    for (WebSocketClientHandler baseHandler : baseHandlers()) {
      String requestKey = baseHandler.getRelayName() + ":" + subscriptionId;
      clientMap.computeIfAbsent(
          requestKey,
          key -> createHandler(requestKey, baseHandler.getRelayUri()));
    }
  }

  public void closeAll() throws IOException {
    for (WebSocketClientHandler client : clientMap.values()) {
      client.close();
    }
  }

  private WebSocketClientHandler createHandler(String relayName, String relayUri) {
    try {
      return factory.create(relayName, relayUri);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException("Failed to initialize WebSocket client handler", e);
    }
  }
}
