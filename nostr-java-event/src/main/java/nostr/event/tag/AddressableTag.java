package nostr.event.tag;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.AddressableTagSerializer;

/**
 *
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "a", nip = 33)
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = AddressableTagSerializer.class)
public class AddressableTag extends BaseTag {

    private Integer kind;
    private PublicKey publicKey;
    private IdentifierTag identifierTag;
    private Relay relay;
}
