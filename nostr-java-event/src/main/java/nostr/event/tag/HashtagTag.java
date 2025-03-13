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
@Tag(code = "t", nip = 12)
@NoArgsConstructor
@AllArgsConstructor
public class HashtagTag extends BaseTag {

    @Key
    @JsonProperty("t")
    private String hashTag;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        HashtagTag tag = new HashtagTag();
        setRequiredField(node.get(1), (n, t) -> tag.setHashTag(n.asText()), tag);
        return (T) tag;
    }
}
