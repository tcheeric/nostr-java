package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.impl.GenericTag;
import nostr.event.json.serializer.ClassifiedListingTagSerializer;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Tag(code = "tags", nip = 99)
@JsonSerialize(using = ClassifiedListingTagSerializer.class)
public class ClassifiedListingTag extends GenericTag {
  @Key
  @JsonProperty
  private String title;

  @Key
  @JsonProperty
  private String summary;

  @Key
  @JsonProperty("published_at")
  @EqualsAndHashCode.Exclude
  private Long publishedAt;

  @Key
  @JsonProperty
  private String location;

  @Key
  @JsonProperty("price")
  private PriceTag priceTag;

  public ClassifiedListingTag(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    super("tags", 99);
    this.title = title;
    this.summary = summary;
    this.priceTag = priceTag;
  }
}