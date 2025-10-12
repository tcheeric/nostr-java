package nostr.api.client;

import nostr.api.WebSocketClientHandler;
import nostr.base.RelayUri;

import java.util.concurrent.ExecutionException;

/**
 * Factory for creating {@link WebSocketClientHandler} instances.
 */
@FunctionalInterface
public interface WebSocketClientHandlerFactory {
  /**
   * Create a handler for the given relay definition.
   *
   * @param relayName logical relay identifier
   * @param relayUri websocket URI of the relay
   * @return initialized handler ready for use
   * @throws ExecutionException if the underlying client initialization fails
   * @throws InterruptedException if thread interruption occurs during initialization
   */
  WebSocketClientHandler create(String relayName, RelayUri relayUri)
      throws ExecutionException, InterruptedException;
}
