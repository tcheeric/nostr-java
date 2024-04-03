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

  private final ClassifiedListing classifiedListing;

  public ClassifiedListingEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull ClassifiedListing classifiedListing) {
    super(sender, 30_402, baseTags, content);
    this.classifiedListing = classifiedListing;
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @JsonSerialize(using = ClassifiedEventSerializer.class)
  public static class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
    @JsonProperty
    private final String id = "REVISIT: CLASSIFIED EVENT using IDENTIFIER TAG thus id req'd as per code 'd' as per NIP-99 spec";

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

    public ClassifiedListing(@NonNull String title, @NonNull String summary, @NonNull List<PriceTag> priceTags) {
      this.title = title;
      this.summary = summary;
      this.priceTags = priceTags;
    }
  }
}
