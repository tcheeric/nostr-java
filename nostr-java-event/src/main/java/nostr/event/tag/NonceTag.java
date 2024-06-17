
package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
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

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        NonceTag tag = new NonceTag();

        final JsonNode nodeNonce = node.get(1);
        if (nodeNonce != null) {
            tag.setNonce(Integer.valueOf(nodeNonce.asText()));
        }

        final JsonNode nodeDifficulty = node.get(2);
        if (nodeDifficulty != null) {
            tag.setDifficulty(Integer.valueOf(nodeDifficulty.asText()));
        }
        return (T) tag;
    }
}
