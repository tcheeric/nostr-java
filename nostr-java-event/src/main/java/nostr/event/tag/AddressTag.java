package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.util.Optional;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.AddressTagSerializer;

import java.util.List;

/** Represents an 'a' addressable/parameterized replaceable tag (NIP-33). */
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
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Relay relay;

  /** Optional accessor for relay. */
  public Optional<Relay> getRelayOptional() {
    return Optional.ofNullable(relay);
  }

  /** Optional accessor for identifierTag. */
  public Optional<IdentifierTag> getIdentifierTagOptional() {
    return Optional.ofNullable(identifierTag);
  }

  public static AddressTag deserialize(@NonNull JsonNode node) {
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
    return tag;
  }

  public static AddressTag updateFields(@NonNull GenericTag tag) {
    if (!"a".equals(tag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for AddressTag");
    }

    AddressTag addressTag = new AddressTag();
    List<ElementAttribute> attributes = tag.getAttributes();
    String attr0 = attributes.get(0).value().toString();
    String[] parts = attr0.split(":");
    Integer kind = Integer.parseInt(parts[0]);
    PublicKey publicKey = new PublicKey(parts[1]);
    String id = parts.length == 3 ? parts[2] : null;

    addressTag.setKind(kind);
    addressTag.setPublicKey(publicKey);
    addressTag.setIdentifierTag(id != null ? new IdentifierTag(id) : null);
    if (tag.getAttributes().size() == 2) {
      addressTag.setRelay(new Relay(tag.getAttributes().get(1).value().toString()));
    }
    return addressTag;
  }
}
