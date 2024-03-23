package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {

  public ClassifiedListingEvent(PublicKey sender, List<BaseTag> tags, IContent content) {
    super(sender, 30_402, tags, content.toString());
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  @JsonSerialize(using = ClassifiedEventSerializer.class)
  public static class ClassifiedListing extends AbstractEventContent<ClassifiedListingEvent> {
    @JsonProperty
    private final String id;
    @JsonProperty
    private String summary;
    @JsonProperty
    private String location;
    @JsonProperty("price")
    private List<PriceTag> priceTags;
    @JsonProperty
    private String title;
    @JsonProperty
    private String currency;

    public ClassifiedListing() {
      this.priceTags = new ArrayList<>();
      this.id = UUID.randomUUID().toString();
    }
  }
}
