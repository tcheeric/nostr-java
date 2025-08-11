package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Marker;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.ReferenceTagSerializer;

import java.net.URI;

/**
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "r", nip = 12)
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = ReferenceTagSerializer.class)
public class ReferenceTag extends BaseTag {

    @Key
    @JsonProperty("uri")
    private URI uri;

    @Key
    private Marker marker;

    public ReferenceTag(@NonNull URI uri) {
        this.uri = uri;
    }

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        ReferenceTag tag = new ReferenceTag();
        setRequiredField(node.get(1), (n, t) -> tag.setUri(URI.create(n.asText())), tag);
        setOptionalField(node.get(2), (n, t) -> tag.setMarker(Marker.valueOf(n.asText().toUpperCase())), tag);
        return (T) tag;
    }

    public static ReferenceTag updateFields(@NonNull GenericTag genericTag) {
        if (!"r".equals(genericTag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for ReferenceTag");
        }

        if (genericTag.getAttributes().size() < 1 || genericTag.getAttributes().size() > 2) {
            throw new IllegalArgumentException("Invalid number of attributes for ReferenceTag");
        }

        ReferenceTag tag = new ReferenceTag();
        tag.setUri(URI.create(genericTag.getAttributes().get(0).value().toString()));
        if (genericTag.getAttributes().size() == 2) {
            tag.setMarker(Marker.valueOf(genericTag.getAttributes().get(1).value().toString().toUpperCase()));
        } else {
            tag.setMarker(null);
        }

        return tag;
    }
}
