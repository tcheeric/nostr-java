package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.base.Marker;

/**
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Tag(code = "e", name = "event")
@JsonPropertyOrder({"idEvent", "recommendedRelayUrl", "marker"})
public class EventTag extends GenericTag {

    @Key
    @JsonProperty("idEvent")
    private String idEvent;

    @Key
    @JsonProperty("recommendedRelayUrl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String recommendedRelayUrl;

    @Key(nip = 10)
    @JsonProperty("marker")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Marker marker;

    public EventTag() {
        super("e");
    }

    public EventTag(String idEvent) {
        this.recommendedRelayUrl = null;
        this.idEvent = idEvent;

        // TODO: This is a bug. The marker should not be set, or at least not like this.
        //this.marker = this.idEvent == null ? Marker.ROOT : Marker.REPLY;
    }

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        EventTag tag = new EventTag();
        setRequiredField(node.get(1), (n, t) -> tag.setIdEvent(n.asText()), tag);
        setOptionalField(node.get(2), (n, t) -> tag.setRecommendedRelayUrl(n.asText()), tag);
        setOptionalField(node.get(3), (n, t) -> tag.setMarker(Marker.valueOf(n.asText().toUpperCase())), tag);
        return (T) tag;
    }

    public static EventTag updateFields(@NonNull GenericTag tag) {
        if (tag instanceof EventTag) {
            return (EventTag) tag;
        }
        if (!"e".equals(tag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for EventTag");
        }
        EventTag eventTag = new EventTag(tag.getAttributes().get(0).getValue().toString());
        if (tag.getAttributes().size() > 1) {
            eventTag.setRecommendedRelayUrl(tag.getAttributes().get(1).getValue().toString());
        }
        if (tag.getAttributes().size() > 2) {
            eventTag.setMarker(Marker.valueOf(tag.getAttributes().get(2).getValue().toString()));
        }

        return eventTag;
    }

}
