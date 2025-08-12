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
        setRequiredField(node.get(1), (n, t) -> tag.setLocation(n.asText()), tag);
        return (T) tag;
    }

    public static GeohashTag updateFields(@NonNull GenericTag genericTag) {
        if (!"g".equals(genericTag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for GeohashTag");
        }

        if (genericTag.getAttributes().size() != 1) {
            throw new IllegalArgumentException("Invalid number of attributes for GeohashTag");
        }

        GeohashTag tag = new GeohashTag();
        tag.setLocation(genericTag.getAttributes().get(0).value().toString());
        return tag;
    }
}
