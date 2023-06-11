
package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import nostr.base.annotation.Key;
import nostr.event.BaseTag;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "nonce", nip = 13)
@JsonPropertyOrder({"nonce", "difficulty"})
@NoArgsConstructor
public class NonceTag extends BaseTag {

    @Key
    @JsonProperty("nonce")
    private Integer nonce;

    @Key
    @JsonProperty("difficulty")
    private Integer difficulty;

    public NonceTag(@NonNull Integer nonce, @NonNull Integer difficulty) {
        this.nonce = nonce;
        this.difficulty = difficulty;
    }
}
