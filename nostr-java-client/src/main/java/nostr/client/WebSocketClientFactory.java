package nostr.client;

import nostr.base.RelayUri;
import nostr.client.springwebsocket.WebSocketClientIF;

import java.util.concurrent.ExecutionException;

/**
 * Abstraction for creating WebSocket clients for relay URIs.
 */
@FunctionalInterface
public interface WebSocketClientFactory {

  WebSocketClientIF create(RelayUri relayUri) throws ExecutionException, InterruptedException;
}
