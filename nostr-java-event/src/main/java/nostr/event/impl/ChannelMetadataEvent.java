package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.base.Marker;
import nostr.event.entities.ChannelProfile;
import nostr.event.tag.EventTag;
import nostr.event.tag.HashtagTag;

import java.util.List;

/**
 * @author guilhermegps
 */
@Event(name = "Channel Metadata", nip = 28)
@NoArgsConstructor
public class ChannelMetadataEvent extends GenericEvent {

    public ChannelMetadataEvent(PublicKey pubKey, List<BaseTag> baseTagList, String content) {
        super(pubKey, Kind.CHANNEL_METADATA, baseTagList, content);
    }

    @SneakyThrows
    public ChannelProfile getChannelProfile() {
        String content = getContent();
        return MAPPER_AFTERBURNER.readValue(content, ChannelProfile.class);
    }

    public String getChannelCreateEventId() {
        return getTags().stream()
                .filter(tag -> "e".equals(tag.getCode()))
                .map(tag -> (EventTag) tag)
                .filter(tag -> tag.getMarker() == Marker.ROOT)
                .map(EventTag::getIdEvent)
                .findFirst()
                .orElseThrow();
    }

    public List<String> getCategories() {
        return getTags().stream()
                .filter(tag -> "t".equals(tag.getCode()))
                .map(tag -> (HashtagTag) tag)
                .map(HashtagTag::getHashTag)
                .toList();
    }

    protected void validateTags() {
        super.validateTags();

        // Check 'e' root - tag
        EventTag rootTag = getTags().stream()
                .filter(tag -> "e".equals(tag.getCode()))
                .map(tag -> (EventTag) tag)
                .filter(tag -> tag.getMarker() == Marker.ROOT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing or invalid `e` root tag."));
    }
}
