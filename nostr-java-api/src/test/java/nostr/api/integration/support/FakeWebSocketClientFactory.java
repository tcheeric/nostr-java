package nostr.api.integration.support;

import lombok.NonNull;
import nostr.base.RelayUri;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.WebSocketClientIF;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * In-memory {@link WebSocketClientFactory} for tests.
 *
 * <p>Produces {@link FakeWebSocketClient} instances keyed by relay URI and caches them so tests
 * can both inject behavior and later inspect what messages were sent.
 */
public class FakeWebSocketClientFactory implements WebSocketClientFactory {

  private final Map<String, FakeWebSocketClient> clients = new ConcurrentHashMap<>();

  /**
   * Returns a cached fake client for the given relay or creates a new one.
   *
   * @param relayUri target relay URI
   * @return a {@link WebSocketClientIF} backed by {@link FakeWebSocketClient}
   */
  @Override
  public WebSocketClientIF create(@NonNull RelayUri relayUri)
      throws ExecutionException, InterruptedException {
    return clients.computeIfAbsent(relayUri.toString(), FakeWebSocketClient::new);
  }

  /**
   * Retrieves a previously created fake client by its relay URI.
   *
   * @param relayUri string form of the relay URI
   * @return the fake client or {@code null} if none was created yet
   */
  public FakeWebSocketClient get(String relayUri) {
    return clients.get(relayUri);
  }
}
