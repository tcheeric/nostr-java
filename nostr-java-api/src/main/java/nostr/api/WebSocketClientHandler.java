package nostr.api;

import lombok.Getter;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class WebSocketClientHandler {
    private final SpringWebSocketClient eventClient;
    private final Map<String, SpringWebSocketClient> requestClientMap = new ConcurrentHashMap<>();

    @Getter
    private String relayName;
    @Getter
    private String relayUri;

    protected WebSocketClientHandler(@NonNull String relayName, @NonNull String relayUri)
        throws ExecutionException, InterruptedException {
        this.relayName = relayName;
        this.relayUri = relayUri;
        this.eventClient = new SpringWebSocketClient(new StandardWebSocketClient(relayUri), relayUri);
    }

    public List<String> sendEvent(@NonNull IEvent event) {
        ((GenericEvent) event).validate();
        try {
            return eventClient.send(new EventMessage(event)).stream().toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send event", e);
        }
    }

    protected List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
        try {
            SpringWebSocketClient client = requestClientMap.get(subscriptionId);
            if (client == null) {
                try {
                    requestClientMap.put(
                        subscriptionId,
                        new SpringWebSocketClient(new StandardWebSocketClient(relayUri), relayUri));
                    client = requestClientMap.get(subscriptionId);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException("Failed to initialize request client", e);
                }
            }
            return client.send(new ReqMessage(subscriptionId, filters));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request", e);
        }
    }

    public void close() throws IOException {
        eventClient.closeSocket();
        for (SpringWebSocketClient client : requestClientMap.values()) {
            client.closeSocket();
        }
    }
}
