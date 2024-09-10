package nostr.client.springwebsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.BaseMessage;
import reactor.core.publisher.Flux;

public class SpringWebSocketClient {
  private final WebSocketClient webSocketClient;

  @Getter
  private final String relayUrl;

  public SpringWebSocketClient(@NonNull String relayUrl) {
    webSocketClient = new WebSocketClient(new WebSocketHandler(), relayUrl);
    this.relayUrl = relayUrl;
  }

  public Flux<String> send(@NonNull BaseMessage eventMessage) throws JsonProcessingException {
    return webSocketClient.sendMessageMono(eventMessage.encode());
  }

  public Flux<String> send(@NonNull String json) {
    return webSocketClient.sendMessageMono(json);
  }

  public Flux<String> close(@NonNull String subscriptionId) {
    return webSocketClient.disconnect(subscriptionId);
  }

  public void closeSocket() {
    webSocketClient.closeSocket();
  }
}

