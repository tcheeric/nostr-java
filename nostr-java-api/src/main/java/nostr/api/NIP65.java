package nostr.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Marker;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

public class NIP65 extends EventNostr {

  public NIP65(@NonNull Identity sender) {
    setSender(sender);
  }

  public NIP65 createRelayListMetadataEvent(@NonNull List<Relay> relayList) {
    List<BaseTag> relayUrlTags = relayList.stream().map(relay -> createRelayUrlTag(relay)).toList();
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Constants.Kind.RELAY_LIST_METADATA_EVENT, relayUrlTags, "")
            .create();
    this.updateEvent(genericEvent);
    return this;
  }

  public NIP65 createRelayListMetadataEvent(
      @NonNull List<Relay> relayList, @NonNull Marker permission) {
    List<BaseTag> relayUrlTags =
        relayList.stream().map(relay -> createRelayUrlTag(relay, permission)).toList();
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Constants.Kind.RELAY_LIST_METADATA_EVENT, relayUrlTags, "")
            .create();
    this.updateEvent(genericEvent);
    return this;
  }

  public NIP65 createRelayListMetadataEvent(@NonNull Map<Relay, Marker> relayMarkerMap) {
    List<BaseTag> relayUrlTags = new ArrayList<>();
    for (Map.Entry<Relay, Marker> entry : relayMarkerMap.entrySet()) {
      relayUrlTags.add(createRelayUrlTag(entry.getKey(), entry.getValue()));
    }
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Constants.Kind.RELAY_LIST_METADATA_EVENT, relayUrlTags, "")
            .create();
    this.updateEvent(genericEvent);
    return this;
  }

  public NIP65 addRelayUrlTag(@NonNull String url, @NonNull Marker permission) {
    this.getEvent().addTag(createRelayUrlTag(url, permission));
    return this;
  }

  public static BaseTag createRelayUrlTag(@NonNull Relay relay) {
    return BaseTag.create("r", relay.getUri());
  }

  public static BaseTag createRelayUrlTag(@NonNull String url) {
    return BaseTag.create("r", url);
  }

  public static BaseTag createRelayUrlTag(@NonNull String url, @NonNull Marker permission) {
    return BaseTag.create("r", url, permission.getValue());
  }

  public static BaseTag createRelayUrlTag(@NonNull Relay relay, @NonNull Marker permission) {
    return BaseTag.create("r", relay.getUri(), permission.getValue());
  }
}
