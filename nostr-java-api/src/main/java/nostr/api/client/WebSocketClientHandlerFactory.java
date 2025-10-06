package nostr.api.client;

import java.util.concurrent.ExecutionException;
import nostr.api.WebSocketClientHandler;

/**
 * Factory for creating {@link WebSocketClientHandler} instances.
 */
@FunctionalInterface
public interface WebSocketClientHandlerFactory {
  WebSocketClientHandler create(String relayName, String relayUri)
      throws ExecutionException, InterruptedException;
}
