package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "l", nip = 32)
@NoArgsConstructor
@AllArgsConstructor
public class LabelTag extends GenericTag {

    @Key
    @JsonProperty("l")
    private String label;

    @Key
    @JsonProperty("L")
    private String nameSpace;

    public LabelTag(@NonNull String label, @NonNull LabelNamespaceTag labelNamespaceTag) {
        this(label, labelNamespaceTag.getNameSpace());
    }

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        LabelTag tag = new LabelTag();
        setRequiredField(node.get(1), (n, t) -> tag.setLabel(n.asText()), tag);
        setRequiredField(node.get(2), (n, t) -> tag.setNameSpace(n.asText()), tag);
        return (T) tag;
    }

    public static LabelTag updateFields(@NonNull GenericTag tag) {
        if (tag instanceof LabelTag) {
            return (LabelTag) tag;
        }
        if (!"l".equals(tag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for LabelTag");
        }
        LabelTag labelTag = new LabelTag();
        labelTag.setLabel(tag.getAttributes().get(0).getValue().toString());
        labelTag.setNameSpace(tag.getAttributes().get(1).getValue().toString());
        return labelTag;
    }
}
