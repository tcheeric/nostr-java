package nostr.api;

import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.context.RequestContext;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.util.List;
import java.util.Map;

public interface NostrIF {
  String getRelayResponse();
  NostrIF setSender(@NonNull Identity sender);
  NostrIF setRelays(@NonNull Map<String, String> relays);
  void close();
  void send(@NonNull IEvent event);
  void send(@NonNull IEvent event, Map<String, String> relays);
  void send(@NonNull Filters filters, @NonNull String subscriptionId);
  void send(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays);
  void send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId);
  void send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays);
  void send(@NonNull BaseMessage message, @NonNull RequestContext context);
  NostrIF sign(@NonNull Identity identity, @NonNull ISignable signable);
  boolean verify(@NonNull GenericEvent event);
  Identity getSender();
  Map<String, String> getRelays();
}
