package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
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

/**
 *
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "g", nip = 12)
@NoArgsConstructor
@AllArgsConstructor
public class GeohashTag extends BaseTag {
    
    @Key
    @JsonProperty("g")
    private String location;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        GeohashTag tag = new GeohashTag();

        final JsonNode nodePubKey = node.get(1);
        if (nodePubKey != null) {
            tag.setLocation(nodePubKey.asText());
        }

        return (T) tag;
    }
}
