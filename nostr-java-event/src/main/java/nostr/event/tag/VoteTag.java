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

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "v", nip = 2112)
@NoArgsConstructor
@AllArgsConstructor
public class VoteTag extends BaseTag {

    @Key
    @JsonProperty
    private Integer vote;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        VoteTag tag = new VoteTag();
        setRequiredField(node.get(1), (n, t) -> tag.setVote(n.asInt()), tag);
        return (T) tag;
    }
}
