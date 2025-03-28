package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.AddressTagSerializer;

/**
 *
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "a", nip = 33)
@JsonPropertyOrder({"kind", "publicKey", "identifierTag", "relay"})
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = AddressTagSerializer.class)
public class AddressTag extends BaseTag {

    @Key
    @JsonProperty
    private Integer kind;

    @Key
    @JsonProperty
    private PublicKey publicKey;

    @Key
    @JsonProperty
    private IdentifierTag identifierTag;

    @Key
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Relay relay;

    public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
        AddressTag tag = new AddressTag();
        setRequiredField(node.get(1), (n, a) -> tag.setKind(n.asInt()), tag);
        setRequiredField(node.get(2), (n, a) -> tag.setPublicKey(new PublicKey(n.asText())), tag);
        setRequiredField(node.get(3), (n, a) -> tag.setIdentifierTag(new IdentifierTag(n.asText())), tag);
        setOptionalField(node.get(4), (n, a) -> tag.setRelay(new Relay(n.asText())), tag);
        return (T) tag;
    }
}
