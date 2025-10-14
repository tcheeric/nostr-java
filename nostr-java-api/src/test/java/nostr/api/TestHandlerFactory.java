package nostr.api;

import lombok.NonNull;
import nostr.base.RelayUri;
import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Test-only factory to construct {@link WebSocketClientHandler} while staying inside the
 * {@code nostr.api} package to access package-private constructor.
 */
public final class TestHandlerFactory {
  private TestHandlerFactory() {}

  public static WebSocketClientHandler create(
      @NonNull String relayName,
      @NonNull String relayUri,
      @NonNull SpringWebSocketClient client,
      @NonNull Function<SubscriptionId, SpringWebSocketClient> requestClientFactory,
      @NonNull WebSocketClientFactory clientFactory) throws ExecutionException, InterruptedException {
    return new WebSocketClientHandler(
        relayName,
        new RelayUri(relayUri),
        client,
        new HashMap<>(),
        requestClientFactory,
        clientFactory);
  }
}

