package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
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

  @Override
  public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    createRequestClient(subscriptionId);

    return clientMap.entrySet().stream().filter(entry ->
            entry.getValue().getRelayName().equals(String.join(entry.getKey(), subscriptionId)))
        .map(Entry::getValue)
        .map(webSocketClientHandler ->
            webSocketClientHandler.sendRequest(
                filters,
                webSocketClientHandler.getRelayName()))
        .flatMap(List::stream).toList();
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

  private void createRequestClient(String subscriptionId) {
    if (clientMap.entrySet().stream() // if a request client doesn't yet exist for subscriptionId...
        .noneMatch(entry ->
            entry.getValue().getRelayName().equals(String.join(entry.getKey(), subscriptionId)))) {
      clientMap.keySet().forEach(clientMapKey -> // ... create one for each relay and add it to the client map
          clientMap.entrySet().stream().map(entry ->
                  new WebSocketClientHandler(
                      String.join(entry.getKey(), subscriptionId),
                      entry.getValue().getRelayUri()))
              .toList().forEach(webSocketClientHandler ->
                  clientMap.put(clientMapKey, webSocketClientHandler)));
    }
  }
}
