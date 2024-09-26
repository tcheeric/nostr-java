package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.context.RequestContext;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import nostr.util.NostrUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class NostrSpringWebSocketClient implements NostrIF {

  private static NostrSpringWebSocketClient INSTANCE;

  private List<SpringWebSocketClient> clients = new ArrayList<>();

  @Getter
  private Identity sender;

  private NostrSpringWebSocketClient(@NonNull Map<String, String> relays) {
    relays.values().forEach(this::createClientForRelay);
  }

  public NostrSpringWebSocketClient(@NonNull Identity sender) {
    this.sender = sender;
  }

  public static NostrIF getInstance() {
    return (INSTANCE == null) ? new NostrSpringWebSocketClient() : INSTANCE;
  }

  public static NostrIF getInstance(@NonNull Identity sender) {
    return (INSTANCE == null) ? new NostrSpringWebSocketClient(sender) : INSTANCE;
  }

  private void createClientForRelay(String relay) {
    clients.add(new SpringWebSocketClient(relay));
  }

  @Override
  public NostrIF setSender(@NonNull Identity sender) {
    this.sender = sender;
    return this;
  }

  @Override
  public NostrIF setRelays(@NonNull Map<String, String> relays) {
    relays.values().stream()
        .filter(relay ->
            clients.stream()
                .map(SpringWebSocketClient::getRelayUrl)
                .noneMatch(
                    relay::equals))
        .forEach(this::addClient);
    return this;
  }

  @SneakyThrows
  private void addClient(String relay) {
    clients.add(new SpringWebSocketClient(relay));
  }

  @Override
  public void close() {
    clients.forEach(SpringWebSocketClient::closeSocket);
  }

  @Override
  public List<String> send(@NonNull IEvent event) {
    EventMessage message = new EventMessage(event, event.getId());
    return clients.stream().flatMap(client -> send(client, message).stream()).toList();
  }


  @Override
  public List<String> send(@NonNull IEvent event, Map<String, String> relays) {
    setRelays(relays);
    return send(event);
  }

  @Override
  public List<String> send(@NonNull Filters filters, @NonNull String subscriptionId) {
    ReqMessage reqMessage = new ReqMessage(subscriptionId, filters);
    return clients.stream().flatMap(client -> send(client, reqMessage).stream()).toList();
  }

  @Override
  public List<String> send(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
    setRelays(relays);
    return send(filters, subscriptionId);
  }

  @Override
  public List<String> send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
    return filtersList.stream().flatMap(filters -> send(filters, subscriptionId).stream()).toList();
  }

  @Override
  public List<String> send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays) {
    return filtersList.stream().flatMap(filters -> send(filters, subscriptionId, relays).stream()).toList();
  }

  @SneakyThrows
  private List<String> send(SpringWebSocketClient client, BaseMessage message) {
    return client.send(message);
  }

  @Override
  public List<String> send(@NonNull BaseMessage message, @NonNull RequestContext context) {
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
  public Map<String, String> getRelays() {
    return clients.stream()
        .collect(Collectors.toMap(SpringWebSocketClient::getRelayUrl, SpringWebSocketClient::getRelayUrl, (prev, next) -> next, HashMap::new));
  }
}
