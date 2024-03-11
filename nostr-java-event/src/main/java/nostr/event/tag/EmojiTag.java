package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/**
 *
 * @author guilhermegps
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "emoji", nip = 30)
@AllArgsConstructor
public class EmojiTag extends BaseTag {

    @Key
    private String shortcode;

    @Key
    @JsonProperty("image-url")
    private String url;
    
}
