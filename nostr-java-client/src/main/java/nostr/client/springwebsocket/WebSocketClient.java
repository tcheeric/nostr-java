package nostr.client.springwebsocket;

import lombok.NonNull;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClient {
  private final WebSocketHandler webSocketHandler;

  public WebSocketClient(@NonNull WebSocketHandler webSocketHandler, @NonNull String relayUrl) {
    this.webSocketHandler = webSocketHandler;
    this.webSocketHandler.connect(new ReactorNettyWebSocketClient(), getURI(relayUrl));
  }

  Flux<String> sendMessageMono(@NonNull String message) {
    return Mono
        .fromRunnable(
            () -> webSocketHandler.send(message)
        )
        .thenMany(
            webSocketHandler.receive().map(String::trim));
  }

  public void closeSocket() {
    webSocketHandler.disconnect();
  }

  public Flux<String> disconnect(@NonNull String subscriptionId) {
    Flux<String> stringFlux = sendMessageMono("[\"CLOSE\",\"" + subscriptionId + "\"]");
    closeSocket();
    return stringFlux;
  }

  private URI getURI(@NonNull String relayUrl) {
    try {
      return new URI(relayUrl);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
