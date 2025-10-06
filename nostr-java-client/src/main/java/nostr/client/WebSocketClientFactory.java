package nostr.client;

import java.util.concurrent.ExecutionException;
import nostr.base.RelayUri;
import nostr.client.springwebsocket.WebSocketClientIF;

/**
 * Abstraction for creating WebSocket clients for relay URIs.
 */
@FunctionalInterface
public interface WebSocketClientFactory {

  WebSocketClientIF create(RelayUri relayUri) throws ExecutionException, InterruptedException;
}
