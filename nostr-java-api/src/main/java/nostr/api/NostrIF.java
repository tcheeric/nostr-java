package nostr.api;

import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Flux;

public interface NostrIF {
  NostrIF setSender(@NonNull Identity sender);

  NostrIF setRelays(@NonNull Map<String, String> relays);

  Flux<String> sendEvent(@NonNull IEvent event);

  Flux<String> sendEvent(@NonNull IEvent event, Map<String, String> relays);

  Flux<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId);

  Flux<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays);

  Flux<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId);

  Flux<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays);
  NostrIF sign(@NonNull Identity identity, @NonNull ISignable signable) throws Exception;
  boolean verify(@NonNull GenericEvent event);
  Identity getSender();
  Map<String, String> getRelays();
  void close() throws IOException;
}
