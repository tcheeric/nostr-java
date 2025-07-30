package nostr.client.springwebsocket;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.BaseMessage;

import java.io.IOException;
import reactor.core.publisher.Flux;

public class SpringWebSocketClient {
  private final WebSocketClientIF webSocketClientIF;

  @Getter
  private final String relayUrl;

  public SpringWebSocketClient(@NonNull String relayUrl) {
    webSocketClientIF = new StandardWebSocketClient(relayUrl);
    this.relayUrl = relayUrl;
  }

  @SneakyThrows
  public Flux<String> send(@NonNull BaseMessage eventMessage) {
    return webSocketClientIF.send(eventMessage.encode());
  }

  public Flux<String> send(@NonNull String json) throws IOException {
    return webSocketClientIF.send(json);
  }

  public void closeSocket() throws IOException {
    webSocketClientIF.closeSocket();
  }
}

