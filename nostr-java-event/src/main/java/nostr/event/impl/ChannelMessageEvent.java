package nostr.event.impl;

import java.util.ArrayList;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.Marker;
import nostr.event.tag.EventTag;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Channel Message", nip = 28)
public class ChannelMessageEvent extends GenericEvent {

    public ChannelMessageEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent rootEvent, String content) {
        super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);
        this.addTag(EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build());
    }

    public ChannelMessageEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent rootEvent, String content, Relay recommendedRelay) {
        super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);
        final EventTag eventTag = EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build();
        if (recommendedRelay != null) {
            eventTag.setRecommendedRelayUrl((recommendedRelay.getUri()));
        }
        this.addTag(eventTag);
    }

    public ChannelMessageEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent rootEvent, @NonNull ChannelMessageEvent replyEvent, String content) {
        super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);
        this.addTag(EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build());
        this.addTag(EventTag.builder().idEvent(replyEvent.getId()).marker(Marker.REPLY).build());
    }

    public ChannelMessageEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent rootEvent, @NonNull ChannelMessageEvent replyEvent, String content, Relay recommendedRelay) {
        this(pubKey, rootEvent, replyEvent, content, recommendedRelay, recommendedRelay);
    }

    public ChannelMessageEvent(@NonNull PublicKey pubKey, ChannelCreateEvent rootEvent, ChannelMessageEvent replyEvent, String content, Relay recommendedRelayRoot, Relay recommendedRelayReply) {
        super(pubKey, Kind.CHANNEL_MESSAGE, new ArrayList<>(), content);

        final EventTag rootEventTag = EventTag.builder().idEvent(rootEvent.getId()).marker(Marker.ROOT).build();
        if (recommendedRelayRoot != null) {
            rootEventTag.setRecommendedRelayUrl(recommendedRelayRoot.getUri());
        }
        this.addTag(rootEventTag);

        final EventTag replyEventTag = EventTag.builder().idEvent(replyEvent.getId()).marker(Marker.REPLY).build();
        if (recommendedRelayReply != null) {
            replyEventTag.setRecommendedRelayUrl(recommendedRelayReply.getUri());
        }
        this.addTag(replyEventTag);
    }
}
