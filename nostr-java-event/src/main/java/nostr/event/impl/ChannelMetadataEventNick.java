package nostr.event.impl;

import lombok.Getter;
import lombok.Setter;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.tag.EventTag;

/**
 * @author guilhermegps
 */
@Getter
@Setter
@Event(name = "Channel Metadata", nip = 28)
public class ChannelMetadataEventNick extends EventDecorator {

  public ChannelMetadataEventNick(GenericEventNick genericEvent) {
    super(genericEvent);
    setKind(Kind.CHANNEL_METADATA);
    addTag(EventTag.builder().idEvent(genericEvent.getId()).build());
  }

//    public ChannelMetadataEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent event, ChannelProfile profile) {
//        super(pubKey, Kind.CHANNEL_METADATA, new ArrayList<>(), escapeJsonString(profile.toString()));
//        this.addTag(EventTag.builder().idEvent(event.getId()).build());
//    }
//
//    public ChannelMetadataEvent(@NonNull PublicKey pubKey, @NonNull EventTag channelCreateEventTag, ChannelProfile profile) {
//        super(pubKey, Kind.CHANNEL_METADATA, new ArrayList<>(), escapeJsonString(profile.toString()));
//        this.addTag(channelCreateEventTag);
}
