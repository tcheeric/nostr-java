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
public class HashtagTag extends GenericTag {

    @Key
    @JsonProperty("t")
    private String hashTag;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        HashtagTag tag = new HashtagTag();
        setRequiredField(node.get(1), (n, t) -> tag.setHashTag(n.asText()), tag);
        return (T) tag;
    }

    public static HashtagTag updateFields(@NonNull GenericTag genericTag) {
        if (genericTag instanceof HashtagTag) {
            return (HashtagTag) genericTag;
        }

        if (!"t".equals(genericTag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for HashtagTag");
        }

        if (genericTag.getAttributes().size() != 1) {
            throw new IllegalArgumentException("Invalid number of attributes for HashtagTag");
        }

        HashtagTag tag = new HashtagTag();
        tag.setHashTag(genericTag.getAttributes().get(0).getValue().toString());
        return tag;
    }
}
