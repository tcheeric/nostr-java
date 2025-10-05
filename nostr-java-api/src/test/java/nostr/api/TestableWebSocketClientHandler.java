package nostr.api;

import java.util.Map;
import java.util.function.Function;
import nostr.client.springwebsocket.SpringWebSocketClient;

public class TestableWebSocketClientHandler extends WebSocketClientHandler {
  public TestableWebSocketClientHandler(
      String relayName,
      String relayUri,
      SpringWebSocketClient eventClient,
      Function<String, SpringWebSocketClient> requestClientFactory) {
    super(relayName, relayUri, eventClient, Map.of(), requestClientFactory);
  }
}
