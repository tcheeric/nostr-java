package nostr.event.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import nostr.base.ChannelProfile;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.list.TagList;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Channel Metadata", nip = 28)
public class ChannelMetadataEvent extends GenericEvent {

    public ChannelMetadataEvent(@NonNull PublicKey pubKey, @NonNull TagList tags, String content) {
        super(pubKey, Kind.CHANNEL_METADATA, tags, content);
    }

    public ChannelMetadataEvent(@NonNull PublicKey pubKey, @NonNull TagList tags, ChannelProfile profile) {
        super(pubKey, Kind.CHANNEL_METADATA, tags);
        this.setContent(profile);
    }

    private void setContent(ChannelProfile profile) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(profile);

            // Escape the JSON string
            String escapedJsonString = escapeJsonString(jsonString);

            this.setContent(escapedJsonString);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
