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

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Tag(code = "u", nip = 61)
public class UrlTag extends BaseTag {

  @Key
  @JsonProperty("u")
  private String url;

  @SuppressWarnings("unchecked")
  public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
    UrlTag tag = new UrlTag();
    setRequiredField(node.get(1), (n, t) -> tag.setUrl(n.asText()), tag);
    return (T) tag;
  }

  public static UrlTag updateFields(@NonNull GenericTag tag) {
    if (!"u".equals(tag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for UrlTag");
    }

    if (tag.getAttributes().size() != 1) {
      throw new IllegalArgumentException("Invalid number of attributes for UrlTag");
    }

    UrlTag urlTag = new UrlTag();
    urlTag.setUrl(tag.getAttributes().get(0).value().toString());

    return urlTag;
  }
}
