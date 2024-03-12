package nostr.event.impl;

import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.Marker;
import nostr.event.tag.EventTag;

/**
 * @author guilhermegps
 */
@Event(name = "Channel Message", nip = 28)
public class ChannelMessageEventNick extends EventDecorator {
  public ChannelMessageEventNick(GenericEventNick genericEvent) {
    super(genericEvent);
    setKind(Kind.CHANNEL_MESSAGE);
    addTag(EventTag.builder().idEvent(genericEvent.getId()).marker(Marker.ROOT).build());
  }

//    public ChannelMessageEvent(GenericEventNick genericEvent, Relay recommendedRelay) {
//        this(genericEvent);
//        if (recommendedRelay != null) {
//            genericEvent.getTags().get(0).setRecommendedRelayUrl((recommendedRelay.getHostname()));
//        }
//        this.addTag(eventTag);
//    }
//
//    public ChannelMessageEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent rootEvent, @NonNull ChannelMessageEvent replyEvent, String content) {
//        super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);
//        this.addTag(EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build());
//        this.addTag(EventTag.builder().idEvent(replyEvent.getId()).marker(Marker.REPLY).build());
//    }
//
//    public ChannelMessageEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent rootEvent, @NonNull ChannelMessageEvent replyEvent, String content, Relay recommendedRelay) {
//        this(pubKey, rootEvent, replyEvent, content, recommendedRelay, recommendedRelay);
//    }

  public ChannelMessageEventNick(GenericEventNick genericEvent, ChannelCreateEventNick rootEvent, ChannelMessageEventNick replyEvent, String content, Relay recommendedRelayRoot, Relay recommendedRelayReply) {
    super(genericEvent);
    setKind(Kind.CHANNEL_MESSAGE);
    setContent(content);

    final EventTag rootEventTag = EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build();
    if (recommendedRelayRoot != null) {
      rootEventTag.setRecommendedRelayUrl(recommendedRelayRoot.getHostname());
    }
    this.addTag(rootEventTag);

    final EventTag replyEventTag = EventTag.builder().idEvent(replyEvent.getId()).marker(Marker.REPLY).build();
    if (recommendedRelayReply != null) {
      replyEventTag.setRecommendedRelayUrl(recommendedRelayReply.getHostname());
    }
    this.addTag(replyEventTag);
  }
}
