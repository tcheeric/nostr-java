package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.api.service.NoteService;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import nostr.util.NostrUtil;
import nostr.api.service.impl.DefaultNoteService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@NoArgsConstructor
public class NostrSpringWebSocketClient implements NostrIF {
    private final Map<String, WebSocketClientHandler> clientMap = new ConcurrentHashMap<>();
    @Getter
    private Identity sender;

    private NoteService noteService = new DefaultNoteService();

    private static volatile NostrSpringWebSocketClient INSTANCE;

    public NostrSpringWebSocketClient(String relayName, String relayUri) {
        setRelays(Map.of(relayName, relayUri));
    }

    public NostrSpringWebSocketClient(@NonNull NoteService noteService) {
        this.noteService = noteService;
    }

    public NostrSpringWebSocketClient(@NonNull Identity sender, @NonNull NoteService noteService) {
        this.sender = sender;
        this.noteService = noteService;
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
        relays.entrySet().forEach(relayEntry -> {
            try {
                clientMap.putIfAbsent(
                    relayEntry.getKey(),
                    new WebSocketClientHandler(relayEntry.getKey(), relayEntry.getValue()));
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Failed to initialize WebSocket client handler", e);
            }
        });
        return this;
    }

    @Override
    public List<String> sendEvent(@NonNull IEvent event) {
        if (event instanceof GenericEvent genericEvent) {
            if (!verify(genericEvent)) {
                throw new IllegalStateException("Event verification failed");
            }
        }

        return noteService.send(event, clientMap);
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

    public static List<String> sendRequest(@NonNull SpringWebSocketClient client,
                                           @NonNull Filters filters,
                                           @NonNull String subscriptionId) throws IOException {
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

    protected WebSocketClientHandler newWebSocketClientHandler(String relayName, String relayUri)
        throws ExecutionException, InterruptedException {
        return new WebSocketClientHandler(relayName, relayUri);
    }

    private void createRequestClient(String subscriptionId) {
        clientMap.entrySet().stream()
                .filter(entry -> !entry.getKey().contains(":"))
                .forEach(entry -> {
                    String requestKey = entry.getKey() + ":" + subscriptionId;
                    clientMap.computeIfAbsent(
                        requestKey,
                        key -> {
                            try {
                                return newWebSocketClientHandler(requestKey, entry.getValue().getRelayUri());
                            } catch (ExecutionException | InterruptedException e) {
                                throw new RuntimeException("Failed to create request client", e);
                            }
                        });
                });
    }
}
