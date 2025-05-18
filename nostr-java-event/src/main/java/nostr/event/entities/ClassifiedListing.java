package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.JsonContent;
import nostr.event.tag.PriceTag;

@Data
@Builder
@JsonDeserialize(builder = ClassifiedListing.ClassifiedListingBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class ClassifiedListing implements JsonContent {
  private String id;
  private final String title;
  private final String summary;

  @EqualsAndHashCode.Exclude
  private Long publishedAt;
  private String location;

  @JsonProperty("price")
  private final PriceTag priceTag;

  public static ClassifiedListingBuilder builder(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    return new ClassifiedListingBuilder()
        .title(title)
        .summary(summary)
        .priceTag(priceTag);
  }
}