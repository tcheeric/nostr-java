package nostr.api;

import lombok.Getter;
import lombok.NonNull;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketClientHandler {
  private final SpringWebSocketClient eventClient;
  private final Map<String, SpringWebSocketClient> requestClientMap = new ConcurrentHashMap<>();

  @Getter
  private String relayName;
  @Getter
  private String relayUri;

  protected WebSocketClientHandler(@NonNull String relayName, @NonNull String relayUri) {
    this.relayName = relayName;
    this.relayUri = relayUri;
    this.eventClient = new SpringWebSocketClient(relayUri);
  }

  protected List<String> sendEvent(@NonNull GenericEvent event) {
    event.validate();
    return eventClient.send(new EventMessage(event)).stream().toList();
  }

  protected List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    return Optional
        .ofNullable(
            requestClientMap.get(subscriptionId))
        .map(client ->
            client.send(new ReqMessage(subscriptionId, filters))).or(() -> {
          requestClientMap.put(subscriptionId, new SpringWebSocketClient(relayUri));
          return Optional.ofNullable(
              requestClientMap.get(subscriptionId).send(
                  new ReqMessage(subscriptionId, filters)));
        })
        .orElse(new ArrayList<>());
  }

  public void close() throws IOException {
    eventClient.closeSocket();
    for (SpringWebSocketClient client : requestClientMap.values()) {
      client.closeSocket();
    }
  }
}
