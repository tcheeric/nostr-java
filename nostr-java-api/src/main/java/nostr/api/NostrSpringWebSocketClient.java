package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.context.RequestContext;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.BaseMessage;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.util.NostrUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NoArgsConstructor
public class NostrSpringWebSocketClient implements NostrIF {
  private final Map<String, WebSocketClientHandler> clientMap = new ConcurrentHashMap<>();
  @Getter
  private Identity sender;

  private static NostrSpringWebSocketClient INSTANCE;

  public NostrSpringWebSocketClient(String relayName, String relayUri) {
    setRelays(Map.of(relayName, relayUri));
  }

  public static NostrIF getInstance() {
    return (INSTANCE == null) ? new NostrSpringWebSocketClient() : INSTANCE;
  }

  public static NostrIF getInstance(@NonNull Identity sender) {
    return (INSTANCE == null) ? new NostrSpringWebSocketClient(sender) : INSTANCE;
  }

  public NostrSpringWebSocketClient(@NonNull Identity sender) {
    this.sender = sender;
  }

  public NostrIF setSender(@NonNull Identity sender) {
    this.sender = sender;
    return this;
  }

  public <T extends GenericEvent> List<String> sendEvent(T event, Map<String, String> relays) {
    setRelays(relays);
    return relays.keySet().stream().map(s ->
            clientMap.get(s).sendEvent(event))
        .flatMap(List::stream)
        .distinct().toList();
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
  public List<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays) {
    return sendRequest(filtersList, subscriptionId);
  }

  @Override
  public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
    return sendRequest(filters, subscriptionId);
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
  public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId2) {
    String subscriptionId = "-" + subscriptionId2;
    Set<Entry<String, WebSocketClientHandler>> entrySet = clientMap.entrySet();

    if (entrySet.stream().noneMatch(entry ->
    {
      String relayName = entry.getValue().getRelayName();
      String targetRelayName = entry.getKey() + subscriptionId;
      boolean equals = relayName.equals(targetRelayName);
      return equals;
    })) {
      clientMap.keySet().forEach(clientMapKey ->
          entrySet.stream().map(entry ->
          {
            String relayName = entry.getKey() + subscriptionId;
            String relayUri = entry.getValue().getRelayUri();
            return new WebSocketClientHandler(relayName, relayUri);
          }).toList().forEach(webSocketClientHandler ->
              clientMap.put(clientMapKey, webSocketClientHandler)));
    }

    List<String> list = entrySet.stream().filter(entry ->
        {
          String relayName = entry.getValue().getRelayName();
          String targetRelayName = entry.getKey() + subscriptionId;
          return relayName.equals(targetRelayName);
        })
        .map(Entry::getValue)
        .map(webSocketClientHandler -> webSocketClientHandler.sendRequest(filters, webSocketClientHandler.getRelayName()))
        .flatMap(List::stream).distinct().toList();

    return list;
  }

  @Override
  public List<String> sendRequest(@NonNull BaseMessage message, @NonNull RequestContext context) {
    return List.of();
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
  public Identity getSender() {
    return sender;
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
}
