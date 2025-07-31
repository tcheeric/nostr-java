package nostr.api;

import lombok.Getter;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Flux;

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

    protected Flux<String> sendEvent(@NonNull IEvent event) {
        ((GenericEvent) event).validate();
        return eventClient.send(new EventMessage(event));
    }

    protected Flux<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
        return Optional
                .ofNullable(requestClientMap.get(subscriptionId))
                .map(client -> client.send(new ReqMessage(subscriptionId, filters)))
                .orElseGet(() -> {
                    requestClientMap.put(subscriptionId, new SpringWebSocketClient(relayUri));
                    return requestClientMap.get(subscriptionId).send(new ReqMessage(subscriptionId, filters));
                });
    }

    public void close() throws IOException {
        eventClient.closeSocket();
        for (SpringWebSocketClient client : requestClientMap.values()) {
            client.closeSocket();
        }
    }
}
