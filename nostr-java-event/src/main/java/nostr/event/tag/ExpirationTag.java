package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.ExpirationTagSerializer;

/** Represents an 'expiration' tag (NIP-40). */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Tag(code = "expiration", name = "Expiration Timestamp", nip = 40)
@NoArgsConstructor
@JsonSerialize(using = ExpirationTagSerializer.class)
public class ExpirationTag extends BaseTag {

  @Key @JsonProperty private Integer expiration;

  public static ExpirationTag deserialize(@NonNull JsonNode node) {
    ExpirationTag tag = new ExpirationTag();
    setRequiredField(node.get(1), (n, t) -> tag.setExpiration(Integer.valueOf(n.asText())), tag);
    return tag;
  }

  public static ExpirationTag updateFields(@NonNull GenericTag tag) {
    if (!"expiration".equals(tag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for ExpirationTag");
    }
    String expiration = tag.getAttributes().get(0).value().toString();
    return new ExpirationTag(Integer.parseInt(expiration));
  }
}
