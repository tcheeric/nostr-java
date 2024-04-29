package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;
import nostr.event.json.serializer.ClassifiedListingSerializer;
import nostr.event.tag.PriceTag;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {

  private final ClassifiedListing classifiedListing;

  public ClassifiedListingEvent(PublicKey sender, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
  }

  public ClassifiedListingEvent(PublicKey sender, Kind kind, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
    super(sender, kind, baseTags, content);
    this.classifiedListing = classifiedListing;
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @JsonSerialize(using = ClassifiedListingSerializer.class)
  public static class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
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
    private PriceTag priceTags;

    public ClassifiedListing(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTags) {
      this.title = title;
      this.summary = summary;
      this.priceTags = priceTags;
    }
  }
}
