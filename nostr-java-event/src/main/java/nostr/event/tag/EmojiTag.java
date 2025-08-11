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
 * @author guilhermegps
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "emoji", nip = 30)
@AllArgsConstructor
@NoArgsConstructor
public class EmojiTag extends BaseTag {

    @Key
    private String shortcode;

    @Key
    @JsonProperty("image-url")
    private String url;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        EmojiTag tag = new EmojiTag();
        setRequiredField(node.get(1), (n, t) -> tag.setShortcode(n.asText()), tag);
        setRequiredField(node.get(2), (n, t) -> tag.setUrl(n.asText()), tag);
        return (T) tag;
    }

    public static EmojiTag updateFields(@NonNull GenericTag tag) {
        if (!"emoji".equals(tag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for EmojiTag");
        }

        String shortcode = tag.getAttributes().get(0).value().toString();
        String url = tag.getAttributes().get(1).value().toString();
        EmojiTag emojiTag = new EmojiTag(shortcode, url);
        return emojiTag;
    }
}
