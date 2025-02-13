package nostr.client.springwebsocket;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.BaseMessage;

import java.io.IOException;
import java.util.List;

public class SpringWebSocketClient {
  private final WebSocketClientIF webSocketClientIF;

  @Getter
  private final String relayUrl;

  public SpringWebSocketClient(@NonNull String relayUrl) {
    webSocketClientIF = new StandardWebSocketClient(relayUrl);
    this.relayUrl = relayUrl;
  }

  @SneakyThrows
  public List<String> send(@NonNull BaseMessage eventMessage) {
    return webSocketClientIF.send(eventMessage.encode());
  }

  public List<String> send(@NonNull String json) throws IOException {
    return webSocketClientIF.send(json);
  }

  public void closeSocket() throws IOException {
    webSocketClientIF.closeSocket();
  }
}

