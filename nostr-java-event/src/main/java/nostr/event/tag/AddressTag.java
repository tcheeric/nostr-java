package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
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
        List<String> list = Arrays.stream(node.get(1).asText().split(":")).toList();

        final AddressTag addressTag = new AddressTag();
        addressTag.setKind(Integer.valueOf(list.get(0)));
        addressTag.setPublicKey(new PublicKey(list.get(1)));
        addressTag.setIdentifierTag(new IdentifierTag(list.get(2)));

        Optional.ofNullable(node.get(2)).ifPresent(relay -> addressTag.setRelay(new Relay(relay.asText())));

        return (T) addressTag;
    }
}
