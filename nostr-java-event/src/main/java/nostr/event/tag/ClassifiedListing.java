package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.AbstractEventContent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.json.serializer.ClassifiedListingSerializer;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = ClassifiedListingSerializer.class)
public class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
  @JsonProperty
  private String id;

  @JsonProperty
  private String title;

  @JsonProperty
  private String summary;

  @JsonProperty("published_at")
  @EqualsAndHashCode.Exclude
  private Long publishedAt;

  @JsonProperty
  private String location;

  @JsonProperty("price")
  private PriceTag priceTag;

  public ClassifiedListing(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    this.title = title;
    this.summary = summary;
    this.priceTag = priceTag;
  }
}