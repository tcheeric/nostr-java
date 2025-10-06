package nostr.api;

import java.util.Map;
import java.util.function.Function;
import nostr.base.RelayUri;
import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.SpringWebSocketClientFactory;

public class TestableWebSocketClientHandler extends WebSocketClientHandler {
  public TestableWebSocketClientHandler(
      String relayName,
      String relayUri,
      SpringWebSocketClient eventClient,
      Function<String, SpringWebSocketClient> requestClientFactory) {
    super(
        relayName,
        new RelayUri(relayUri),
        eventClient,
        Map.of(),
        requestClientFactory != null ? id -> requestClientFactory.apply(id.value()) : null,
        new SpringWebSocketClientFactory());
  }
}
