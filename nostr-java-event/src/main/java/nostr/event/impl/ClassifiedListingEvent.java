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
import nostr.event.IContent;
import nostr.event.NIP99Event;
import nostr.event.json.serializer.ClassifiedEventSerializer;
import nostr.event.tag.PriceTag;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {

  private final List<ClassifiedListing> classifiedListings;

  public ClassifiedListingEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull IContent content, @NonNull ClassifiedListing... classifiedListings) {
    super(sender, 30_402, baseTags, content.toString());
    this.classifiedListings = List.of(classifiedListings);
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull IContent content, @NonNull List<ClassifiedListing> classifiedListings) {
    super(sender, 30_402, baseTags, content.toString());
    this.classifiedListings = classifiedListings;
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @JsonSerialize(using = ClassifiedEventSerializer.class)
  public static class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
    @JsonProperty
    private final String id;

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
    private List<PriceTag> priceTags;

    public ClassifiedListing() {
      this.priceTags = new ArrayList<>();
      this.id = "REVISIT: CLASSIFIED EVENT using IDENTIFIER TAG thus id req'd as per code 'd' as per NIP-99 spec";
    }
  }
}
