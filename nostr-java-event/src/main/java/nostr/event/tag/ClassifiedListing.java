package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.annotation.Key;
import nostr.event.AbstractEventContent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.json.serializer.ClassifiedListingTagSerializer;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = ClassifiedListingTagSerializer.class)
public class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
  @JsonProperty
  private String id;

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

  public ClassifiedListing(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    this.title = title;
    this.summary = summary;
    this.priceTag = priceTag;
  }
}