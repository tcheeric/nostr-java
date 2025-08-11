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
@Tag(code = "L", nip = 32)
@NoArgsConstructor
@AllArgsConstructor
public class LabelNamespaceTag extends BaseTag {

    @Key
    @JsonProperty("L")
    private String nameSpace;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        LabelNamespaceTag tag = new LabelNamespaceTag();
        setRequiredField(node.get(1), (n, t) -> tag.setNameSpace(n.asText()), tag);
        return (T) tag;
    }

    public static LabelNamespaceTag updateFields(@NonNull GenericTag tag) {
        LabelNamespaceTag labelNamespaceTag = new LabelNamespaceTag(tag.getAttributes().get(0).value().toString());
        return labelNamespaceTag;
    }
}
