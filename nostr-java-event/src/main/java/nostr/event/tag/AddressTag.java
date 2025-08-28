package nostr.event.tag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.AddressTagSerializer;

/**
 * @author eric
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "a", nip = 33)
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = AddressTagSerializer.class)
public class AddressTag extends BaseTag {

  private Integer kind;
  private PublicKey publicKey;
  private IdentifierTag identifierTag;
  private Relay relay;

  public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
    AddressTag tag = new AddressTag();

    String[] parts = node.get(1).asText().split(":");
    tag.setKind(Integer.valueOf(parts[0]));
    tag.setPublicKey(new PublicKey(parts[1]));
    if (parts.length == 3) {
      tag.setIdentifierTag(new IdentifierTag(parts[2]));
    }

    if (node.size() == 3) {
      tag.setRelay(new Relay(node.get(2).asText()));
    }
    return (T) tag;
  }

  public static AddressTag updateFields(@NonNull GenericTag tag) {
    if (!"a".equals(tag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for AddressTag");
    }

    AddressTag addressTag = new AddressTag();
    List<ElementAttribute> attributes = tag.getAttributes();
    String attr0 = attributes.get(0).value().toString();
    Integer kind = Integer.parseInt(attr0.split(":")[0]);
    PublicKey publicKey = new PublicKey(attr0.split(":")[1]);
    String id = attr0.split(":").length == 3 ? attr0.split(":")[2] : null;

    addressTag.setKind(kind);
    addressTag.setPublicKey(publicKey);
    addressTag.setIdentifierTag(id != null ? new IdentifierTag(id) : null);
    if (tag.getAttributes().size() == 2) {
      addressTag.setRelay(new Relay(tag.getAttributes().get(1).value().toString()));
    }
    return addressTag;
  }
}
