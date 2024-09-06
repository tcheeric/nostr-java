package nostr.client.springwebsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import nostr.event.BaseMessage;
import reactor.core.publisher.Flux;

public class SpringWebSocketClient {
  private final WebSocketClient webSocketClient;

  public SpringWebSocketClient(@NonNull String relayUrl) {
    webSocketClient = new WebSocketClient(new WebSocketHandler(), relayUrl);
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

