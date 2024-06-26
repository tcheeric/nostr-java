/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.Marker;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Tag(code = "e", name = "event")
@JsonPropertyOrder({"idEvent", "recommendedRelayUrl", "marker"})
@NoArgsConstructor
public class EventTag extends BaseTag {

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

    public EventTag(String idEvent) {
        this.recommendedRelayUrl = null;
        this.idEvent = idEvent;
        this.marker = this.idEvent == null ? Marker.ROOT : Marker.REPLY;
    }

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        EventTag tag = new EventTag();

        final JsonNode nodeIdEvent = node.get(1);
        if (nodeIdEvent != null) {
            tag.setIdEvent(nodeIdEvent.asText());
        }

        final JsonNode nodeRelay = node.get(2);
        if (nodeRelay != null) {
            tag.setRecommendedRelayUrl(nodeRelay.asText());
        }

        final JsonNode nodeMarker = node.get(3);
        if (nodeMarker != null) {
            tag.setMarker(Marker.valueOf(nodeMarker.asText().toUpperCase()));
        }
        return (T) tag;
    }
}
