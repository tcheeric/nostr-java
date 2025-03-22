package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/**
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
        setRequiredField(node.get(1), (n, t) -> tag.setNonce(Integer.valueOf(n.asText())), tag);
        setRequiredField(node.get(2), (n, t) -> tag.setDifficulty(Integer.valueOf(n.asText())), tag);
        return (T) tag;
    }
}
