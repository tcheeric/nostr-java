package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {
  @JsonIgnore
  private final ClassifiedListing classifiedListing;

  public ClassifiedListingEvent(@NonNull PublicKey sender, @NonNull Kind kind, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    super(sender, kind, baseTags, content);
    this.classifiedListing = classifiedListing;
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, new ClassifiedListing(title, summary, priceTag));
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull String title, @NonNull String summary, @NonNull BigDecimal number, @NonNull String currency, @NonNull String frequency) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, new ClassifiedListing(title, summary, new PriceTag(number, currency, frequency)));
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
    private PriceTag priceTag;

    public ClassifiedListing(@NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
      this.title = title;
      this.summary = summary;
      this.priceTag = priceTag;
    }
  }
}
