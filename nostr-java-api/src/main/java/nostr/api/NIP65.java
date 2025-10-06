package nostr.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Kind;
import nostr.base.Marker;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * NIP-65 helpers (Relay List Metadata). Build relay list events and r-tags.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/65.md">NIP-65</a>
 */
public class NIP65 extends EventNostr {

  public NIP65(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a relay list metadata event (kind 10002) with a set of relay URLs.
   *
   * @param relayList the list of relays to include
   * @return this instance for chaining
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP65 createRelayListMetadataEvent(@NonNull List<Relay> relayList) {
    List<BaseTag> relayUrlTags = relayList.stream().map(relay -> createRelayUrlTag(relay)).toList();
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.RELAY_LIST_METADATA.getValue(), relayUrlTags, "")
            .create();
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a relay list metadata event (kind 10002) with a permission marker.
   *
   * @param relayList the list of relays to include
   * @param permission the marker indicating read/write preference
   * @return this instance for chaining
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP65 createRelayListMetadataEvent(
      @NonNull List<Relay> relayList, @NonNull Marker permission) {
    List<BaseTag> relayUrlTags =
        relayList.stream().map(relay -> createRelayUrlTag(relay, permission)).toList();
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.RELAY_LIST_METADATA.getValue(), relayUrlTags, "")
            .create();
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a relay list metadata event (kind 10002) from a map of relays to markers.
   *
   * @param relayMarkerMap map from relay to permission marker
   * @return this instance for chaining
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP65 createRelayListMetadataEvent(@NonNull Map<Relay, Marker> relayMarkerMap) {
    List<BaseTag> relayUrlTags = new ArrayList<>();
    for (Map.Entry<Relay, Marker> entry : relayMarkerMap.entrySet()) {
      relayUrlTags.add(createRelayUrlTag(entry.getKey(), entry.getValue()));
    }
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.RELAY_LIST_METADATA.getValue(), relayUrlTags, "")
            .create();
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Add a relay URL tag with permission marker to the current event.
   *
   * @param url the relay URL
   * @param permission the marker indicating read/write preference
   * @return this instance for chaining
   */
  public NIP65 addRelayUrlTag(@NonNull String url, @NonNull Marker permission) {
    this.getEvent().addTag(createRelayUrlTag(url, permission));
    return this;
  }

  /**
   * Create an {@code r} tag for a relay URL.
   *
   * @param relay the relay
   * @return the created tag
   */
  public static BaseTag createRelayUrlTag(@NonNull Relay relay) {
    return BaseTag.create("r", relay.getUri());
  }

  /**
   * Create an {@code r} tag for a relay URL.
   *
   * @param url the relay URL
   * @return the created tag
   */
  public static BaseTag createRelayUrlTag(@NonNull String url) {
    return BaseTag.create("r", url);
  }

  /**
   * Create an {@code r} tag with a permission marker.
   *
   * @param url the relay URL
   * @param permission the marker indicating read/write preference
   * @return the created tag
   */
  public static BaseTag createRelayUrlTag(@NonNull String url, @NonNull Marker permission) {
    return BaseTag.create("r", url, permission.getValue());
  }

  /**
   * Create an {@code r} tag for a relay with a permission marker.
   *
   * @param relay the relay instance
   * @param permission the marker indicating read/write preference
   * @return the created tag
   */
  public static BaseTag createRelayUrlTag(@NonNull Relay relay, @NonNull Marker permission) {
    return BaseTag.create("r", relay.getUri(), permission.getValue());
  }
}
