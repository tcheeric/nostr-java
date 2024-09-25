package nostr.client.springwebsocket;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.BaseMessage;

import java.io.IOException;
import java.util.List;

public class SpringWebSocketClient {
  private final WebSocketClient webSocketClient;

  @Getter
  private final String relayUrl;

  public SpringWebSocketClient(@NonNull String relayUrl) {
    webSocketClient = new StandardWebSocketClient(relayUrl);
    this.relayUrl = relayUrl;
  }

  @SneakyThrows
  public List<String> send(@NonNull BaseMessage eventMessage) {
    return webSocketClient.send(eventMessage.encode());
  }

  public List<String> send(@NonNull String json) throws IOException {
    return webSocketClient.send(json);
  }

  public void close(@NonNull String subscriptionId) {
//    return webSocketClient.disconnect(subscriptionId);
  }

  public void closeSocket() {
//    webSocketClient.closeSocket();
  }
}

