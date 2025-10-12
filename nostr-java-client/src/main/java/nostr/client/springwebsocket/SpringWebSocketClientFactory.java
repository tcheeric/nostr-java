package nostr.client.springwebsocket;

import nostr.base.RelayUri;
import nostr.client.WebSocketClientFactory;

import java.util.concurrent.ExecutionException;

/**
 * Default factory creating Spring-based WebSocket clients.
 */
public class SpringWebSocketClientFactory implements WebSocketClientFactory {

  @Override
  public WebSocketClientIF create(RelayUri relayUri)
      throws ExecutionException, InterruptedException {
    return new StandardWebSocketClient(relayUri.value());
  }
}
