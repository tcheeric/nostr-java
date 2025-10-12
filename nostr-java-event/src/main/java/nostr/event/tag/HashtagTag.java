package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

/** Represents a 't' hashtag tag (NIP-12). */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"t"})
@Tag(code = "t", nip = 12)
@NoArgsConstructor
@AllArgsConstructor
public class HashtagTag extends BaseTag {

  @Key
  @JsonProperty("t")
  private String hashTag;

  public static HashtagTag deserialize(@NonNull JsonNode node) {
    HashtagTag tag = new HashtagTag();
    setRequiredField(node.get(1), (n, t) -> tag.setHashTag(n.asText()), tag);
    return tag;
  }

  public static HashtagTag updateFields(@NonNull GenericTag genericTag) {
    if (!"t".equals(genericTag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for HashtagTag");
    }

    if (genericTag.getAttributes().size() != 1) {
      throw new IllegalArgumentException("Invalid number of attributes for HashtagTag");
    }
    return new HashtagTag(genericTag.getAttributes().get(0).value().toString());
  }
}
