package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.impl.Filters;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class WebSocketClientHandler {

  private SpringWebSocketClient eventClient;
  private Map<String, SpringWebSocketClient> requestClientMap = new ConcurrentHashMap<>();

  @Getter
  private String relayName;
  @Getter
  private String relayUri;

  protected WebSocketClientHandler(@NonNull String relayName, @NonNull String relayUri) {
    this.relayName = relayName;
    this.relayUri = relayUri;
    this.eventClient = new SpringWebSocketClient(relayUri);
  }

  protected List<String> sendEvent(@NonNull IEvent event) {
    return eventClient.send(new EventMessage(event)).stream().toList();
  }

  protected List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    return requestClientMap.get(subscriptionId).send(new ReqMessage(subscriptionId, filters));
  }

  public void close() throws IOException {
    eventClient.closeSocket();
    for (SpringWebSocketClient client : requestClientMap.values()) {
      client.closeSocket();
    }
  }
}
