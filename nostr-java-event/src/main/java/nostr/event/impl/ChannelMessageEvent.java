package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.Marker;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.EventTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guilhermegps
 */
@Event(name = "Channel Message", nip = 28)
@NoArgsConstructor
public class ChannelMessageEvent extends GenericEvent {

  public ChannelMessageEvent(PublicKey pubKey, List<BaseTag> baseTags, String content) {
    super(pubKey, Kind.CHANNEL_MESSAGE, baseTags, content);
  }

  public String getChannelCreateEventId() {
    return nostr.event.filter.Filterable.getTypeSpecificTags(EventTag.class, this).stream()
        .filter(tag -> tag.getMarkerOptional().filter(m -> m == Marker.ROOT).isPresent())
        .map(EventTag::getIdEvent)
        .findFirst()
        .orElseThrow();
  }

  public String getChannelMessageReplyEventId() {
    return nostr.event.filter.Filterable.getTypeSpecificTags(EventTag.class, this).stream()
        .filter(tag -> tag.getMarkerOptional().filter(m -> m == Marker.REPLY).isPresent())
        .map(EventTag::getIdEvent)
        .findFirst()
        .orElse(null);
  }

  public Relay getRootRecommendedRelay() {
    return nostr.event.filter.Filterable.getTypeSpecificTags(EventTag.class, this).stream()
        .filter(tag -> tag.getMarkerOptional().filter(m -> m == Marker.ROOT).isPresent())
        .map(EventTag::getRecommendedRelayUrlOptional)
        .flatMap(java.util.Optional::stream)
        .map(Relay::new)
        .findFirst()
        .orElse(null);
  }

  public Relay getReplyRecommendedRelay(@NonNull String eventId) {
    return nostr.event.filter.Filterable.getTypeSpecificTags(EventTag.class, this).stream()
        .filter(tag -> tag.getMarkerOptional().filter(m -> m == Marker.REPLY).isPresent()
            && tag.getIdEvent().equals(eventId))
        .map(EventTag::getRecommendedRelayUrlOptional)
        .flatMap(java.util.Optional::stream)
        .map(Relay::new)
        .findFirst()
        .orElse(null);
  }

  public void validate() {
    super.validate();

    // Check 'e' root - tag
    EventTag rootTag =
        nostr.event.filter.Filterable.getTypeSpecificTags(EventTag.class, this).stream()
            .filter(tag -> tag.getMarkerOptional().filter(m -> m == Marker.ROOT).isPresent())
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing or invalid `e` root tag."));
  }

  public ChannelMessageEvent(
      @NonNull PublicKey pubKey,
      @NonNull ChannelCreateEvent rootEvent,
      String content,
      Relay recommendedRelay) {
    super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);
    final EventTag eventTag =
        EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build();
    if (recommendedRelay != null) {
      eventTag.setRecommendedRelayUrl((recommendedRelay.getUri()));
    }
    this.addTag(eventTag);
  }

  public ChannelMessageEvent(
      @NonNull PublicKey pubKey,
      @NonNull ChannelCreateEvent rootEvent,
      @NonNull ChannelMessageEvent replyEvent,
      String content) {
    super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);
    this.addTag(EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build());
    this.addTag(EventTag.builder().idEvent(replyEvent.getId()).marker(Marker.REPLY).build());
  }

  public ChannelMessageEvent(
      @NonNull PublicKey pubKey,
      @NonNull ChannelCreateEvent rootEvent,
      @NonNull ChannelMessageEvent replyEvent,
      String content,
      Relay recommendedRelay) {
    this(pubKey, rootEvent, replyEvent, content, recommendedRelay, recommendedRelay);
  }

  public ChannelMessageEvent(
      @NonNull PublicKey pubKey,
      ChannelCreateEvent rootEvent,
      ChannelMessageEvent replyEvent,
      String content,
      Relay recommendedRelayRoot,
      Relay recommendedRelayReply) {
    super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);

    final EventTag rootEventTag =
        EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build();
    if (recommendedRelayRoot != null) {
      rootEventTag.setRecommendedRelayUrl(recommendedRelayRoot.getUri());
    }
    this.addTag(rootEventTag);

    final EventTag replyEventTag =
        EventTag.builder().idEvent(replyEvent.getId()).marker(Marker.REPLY).build();
    if (recommendedRelayReply != null) {
      replyEventTag.setRecommendedRelayUrl(recommendedRelayReply.getUri());
    }
    this.addTag(replyEventTag);
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.CHANNEL_MESSAGE.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.CHANNEL_MESSAGE.getValue());
    }
  }
}
