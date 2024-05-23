package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.AbstractEventContent;
import nostr.event.tag.PriceTag;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
  @JsonIgnore
  private String id;
  @JsonProperty
  private String title;
  @JsonProperty
  private String summary;
  @JsonProperty
  private Long publishedAt;
  @JsonProperty
  private String location;
  @JsonIgnore
  private PriceTag priceTag;

  public ClassifiedListing(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    this.title = title;
    this.summary = summary;
    this.priceTag = priceTag;
  }
}