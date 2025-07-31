package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import nostr.util.NostrUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NoArgsConstructor
public class NostrSpringWebSocketClient implements NostrIF {
    private final Map<String, WebSocketClientHandler> clientMap = new ConcurrentHashMap<>();
    @Getter
    private Identity sender;

    private static volatile NostrSpringWebSocketClient INSTANCE;

    public NostrSpringWebSocketClient(String relayName, String relayUri) {
        setRelays(Map.of(relayName, relayUri));
    }

    public static NostrIF getInstance() {
        if (INSTANCE == null) {
            synchronized (NostrSpringWebSocketClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NostrSpringWebSocketClient();
                }
            }
        }
        return INSTANCE;
    }

    public static NostrIF getInstance(@NonNull Identity sender) {
        if (INSTANCE == null) {
            synchronized (NostrSpringWebSocketClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NostrSpringWebSocketClient(sender);
                } else if (INSTANCE.getSender() == null) {
                    INSTANCE.sender = sender; // Initialize sender if not already set
                }
            }
        }
        return INSTANCE;
    }

    public NostrSpringWebSocketClient(@NonNull Identity sender) {
        this.sender = sender;
    }

    public NostrIF setSender(@NonNull Identity sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public NostrIF setRelays(@NonNull Map<String, String> relays) {
        relays.entrySet().stream().forEach(relayEntry ->
                clientMap.putIfAbsent(relayEntry.getKey(),
                        new WebSocketClientHandler(
                                relayEntry.getKey(),
                                relayEntry.getValue())));
        return this;
    }

    @Override
    public List<String> sendEvent(@NonNull IEvent event) {
        return clientMap.values().stream().map(client ->
                client.sendEvent(event)).flatMap(List::stream).distinct().toList();
    }

    @Override
    public List<String> sendEvent(@NonNull IEvent event, Map<String, String> relays) {
        setRelays(relays);
        return sendEvent(event);
    }

    @Override
    public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
        return sendRequest(List.of(filters), subscriptionId, relays);
    }

    @Override
    public List<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays) {
        setRelays(relays);
        return sendRequest(filtersList, subscriptionId);
    }

    @Override
    public List<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
        return filtersList.stream().map(filters -> sendRequest(
                        filters,
                        subscriptionId
                ))
                .flatMap(List::stream)
                .distinct().toList();
    }

    public static List<String> sendRequest(@NonNull SpringWebSocketClient client, @NonNull Filters filters, @NonNull String subscriptionId) {
        return client.send(new ReqMessage(subscriptionId, filters));
    }


    @Override
    public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
        createRequestClient(subscriptionId);

        return clientMap.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(":" + subscriptionId))
                .map(Entry::getValue)
                .map(webSocketClientHandler ->
                        webSocketClientHandler.sendRequest(
                                filters,
                                webSocketClientHandler.getRelayName()))
                .flatMap(List::stream)
                .toList();
    }


    @Override
    public NostrIF sign(@NonNull Identity identity, @NonNull ISignable signable) {
        identity.sign(signable);
        return this;
    }

    @Override
    public boolean verify(@NonNull GenericEvent event) {
        if (!event.isSigned()) {
            throw new IllegalStateException("The event is not signed");
        }

        var signature = event.getSignature();

        try {
            var message = NostrUtil.sha256(event.get_serializedEvent());
            return Schnorr.verify(message, event.getPubKey().getRawData(), signature.getRawData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getRelays() {
        return clientMap.values().stream()
                .collect(Collectors.toMap(WebSocketClientHandler::getRelayName, WebSocketClientHandler::getRelayUri,
                        (prev, next) -> next, HashMap::new));
    }

    public void close() throws IOException {
        for (WebSocketClientHandler client : clientMap.values()) {
            client.close();
        }
    }

    protected WebSocketClientHandler newWebSocketClientHandler(String relayName, String relayUri) {
        return new WebSocketClientHandler(relayName, relayUri);
    }

    private void createRequestClient(String subscriptionId) {
        clientMap.entrySet().stream()
                .filter(entry -> !entry.getKey().contains(":"))
                .forEach(entry -> {
                    String requestKey = entry.getKey() + ":" + subscriptionId;
                    clientMap.computeIfAbsent(requestKey,
                            key -> newWebSocketClientHandler(requestKey, entry.getValue().getRelayUri()));
                });
    }
}
