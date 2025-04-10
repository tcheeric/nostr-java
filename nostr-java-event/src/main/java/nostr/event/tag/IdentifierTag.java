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
@Tag(code = "d", nip = 33)
@NoArgsConstructor
@AllArgsConstructor
public class IdentifierTag extends BaseTag {

    @Key
    @JsonProperty
    private String uuid;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        IdentifierTag tag = new IdentifierTag();
        setRequiredField(node.get(1), (n, t) -> tag.setUuid(n.asText()), tag);
        return (T) tag;
    }
}
