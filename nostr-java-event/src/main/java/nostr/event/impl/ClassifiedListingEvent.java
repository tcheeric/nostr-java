package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;
import nostr.event.tag.PriceTag;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {
//  private final ClassifiedListing classifiedListing;

  public ClassifiedListingEvent(PublicKey sender, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
  }

  public ClassifiedListingEvent(PublicKey sender, Kind kind, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
    super(sender, kind, baseTags, content);
//    this.classifiedListing = classifiedListing;
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
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
}
