package nostr.api.client;

import nostr.api.WebSocketClientHandler;
import nostr.base.RelayUri;
import nostr.base.SubscriptionId;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Manages the lifecycle of {@link WebSocketClientHandler} instances keyed by relay name.
 */
public class NostrRelayRegistry {

  private final Map<String, WebSocketClientHandler> clientMap = new ConcurrentHashMap<>();
  private final WebSocketClientHandlerFactory factory;

  /**
   * Create a registry backed by the supplied handler factory.
   *
   * @param factory factory used to lazily create relay handlers
   */
  public NostrRelayRegistry(WebSocketClientHandlerFactory factory) {
    this.factory = factory;
  }

  /**
   * Expose the internal handler map for read-only scenarios.
   *
   * @return relay name to handler map
   */
  public Map<String, WebSocketClientHandler> getClientMap() {
    return clientMap;
  }

  /**
   * Ensure handlers exist for the provided relay definitions.
   *
   * @param relays mapping of relay names to relay URIs
   */
  public void registerRelays(Map<String, String> relays) {
    for (Entry<String, String> relayEntry : relays.entrySet()) {
      clientMap.computeIfAbsent(
          relayEntry.getKey(),
          key -> createHandler(key, new RelayUri(relayEntry.getValue())));
    }
  }

  /**
   * Take a snapshot of the currently registered relay URIs.
   *
   * @return immutable copy of relay name to URI mappings
   */
  public Map<String, String> snapshotRelays() {
    return clientMap.values().stream()
        .collect(
            Collectors.toMap(
                WebSocketClientHandler::getRelayName,
                handler -> handler.getRelayUri().toString(),
                (prev, next) -> next,
                HashMap::new));
  }

  /**
   * Return handlers that correspond to base relay connections (non request-scoped).
   *
   * @return list of base handlers
   */
  public List<WebSocketClientHandler> baseHandlers() {
    return clientMap.entrySet().stream()
        .filter(entry -> !entry.getKey().contains(":"))
        .map(Entry::getValue)
        .toList();
  }

  /**
   * Retrieve handlers dedicated to the provided subscription identifier.
   *
   * @param subscriptionId subscription identifier suffix
   * @return list of handlers for the subscription
   */
  public List<WebSocketClientHandler> requestHandlers(SubscriptionId subscriptionId) {
    return clientMap.entrySet().stream()
        .filter(entry -> entry.getKey().endsWith(":" + subscriptionId.value()))
        .map(Entry::getValue)
        .toList();
  }

  /**
   * Create request-scoped handlers for each base relay if they do not already exist.
   *
   * @param subscriptionId subscription identifier used to scope handlers
   */
  public void ensureRequestClients(SubscriptionId subscriptionId) {
    for (WebSocketClientHandler baseHandler : baseHandlers()) {
      clientMap.computeIfAbsent(
          baseHandler.getRelayName() + ":" + subscriptionId.value(),
          key -> createHandler(key, baseHandler.getRelayUri()));
    }
  }

  /**
   * Close all handlers currently registered with the registry.
   *
   * @throws IOException if closing any handler fails
   */
  public void closeAll() throws IOException {
    for (WebSocketClientHandler client : clientMap.values()) {
      client.close();
    }
  }

  private WebSocketClientHandler createHandler(String relayName, RelayUri relayUri) {
    try {
      return factory.create(relayName, relayUri);
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException("Failed to initialize WebSocket client handler", e);
    }
  }
}
