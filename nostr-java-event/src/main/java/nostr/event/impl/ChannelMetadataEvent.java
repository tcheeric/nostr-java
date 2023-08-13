package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import lombok.NonNull;
import nostr.base.ChannelProfile;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.tag.EventTag;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Channel Metadata", nip = 28)
public class ChannelMetadataEvent extends GenericEvent {

    public ChannelMetadataEvent(@NonNull PublicKey pubKey, @NonNull ChannelCreateEvent event, ChannelProfile profile) {
        super(pubKey, Kind.CHANNEL_METADATA, new ArrayList<>(), escapeJsonString(profile.toString()));
        this.addTag(EventTag.builder().idEvent(event.getId()).build());
    }

    public ChannelMetadataEvent(@NonNull PublicKey pubKey, @NonNull EventTag channelCreateEventTag, ChannelProfile profile) {
        super(pubKey, Kind.CHANNEL_METADATA, new ArrayList<>(), escapeJsonString(profile.toString()));
        this.addTag(channelCreateEventTag);
    }
}
