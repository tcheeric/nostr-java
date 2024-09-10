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
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class NostrSpringWebSocketClient implements NostrIF, Subscriber<String> {

  private static NostrSpringWebSocketClient INSTANCE;

  private List<SpringWebSocketClient> clients = new ArrayList<>();
  private Subscription subscription;

  @Getter
  private String relayResponse = null;

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
        .forEach(relay -> clients.add(new SpringWebSocketClient(relay)));
    return this;
  }

  @Override
  public void close() {
    clients.forEach(SpringWebSocketClient::closeSocket);
  }

  @Override
  public void send(@NonNull IEvent event) {
    EventMessage message = new EventMessage(event, event.getId());
    clients.forEach(client -> send(client, message));
  }

  @SneakyThrows
  private NostrSpringWebSocketClient send(SpringWebSocketClient client, BaseMessage message) {
    return client.send(message).subscribeWith(this);
  }

  @Override
  public void send(@NonNull IEvent event, Map<String, String> relays) {
    setRelays(relays);
    send(event);
  }

  @Override
  public void send(@NonNull Filters filters, @NonNull String subscriptionId) {
    ReqMessage reqMessage = new ReqMessage(subscriptionId, filters);
    clients.forEach(client -> send(client, reqMessage));
  }

  @Override
  public void send(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
    setRelays(relays);
    send(filters, subscriptionId);
  }

  @Override
  public void send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
    filtersList.forEach(filters -> send(filters, subscriptionId));
  }

  @Override
  public void send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays) {
    filtersList.forEach(filters -> send(filters, subscriptionId, relays));
  }

  @Override
  public void send(@NonNull BaseMessage message, @NonNull RequestContext context) {
    // NO-OP
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

  @Override
  public void onSubscribe(Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);
  }

  @Override
  public void onNext(String s) {
    subscription.request(1);
    relayResponse = s;
  }

  @Override
  public void onError(Throwable throwable) {
  }

  @Override
  public void onComplete() {
  }
}
