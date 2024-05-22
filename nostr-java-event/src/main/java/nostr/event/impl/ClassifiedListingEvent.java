package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;
import nostr.event.json.serializer.ClassifiedListingSerializer;
import nostr.event.tag.PriceTag;

import java.util.List;

@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {
  public ClassifiedListingEvent(PublicKey sender, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
  }

  public ClassifiedListingEvent(PublicKey sender, Kind kind, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
    super(sender, kind, baseTags, content);
    this.addTag(classifiedListing);
  }

  @Setter
  @Getter
  @EqualsAndHashCode(callSuper = true)
  @Tag(code = "tags", nip = 99)
  @JsonSerialize(using = ClassifiedListingSerializer.class)
  public static class ClassifiedListing extends GenericTag {
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
      super("tags", 99);
      this.title = title;
      this.summary = summary;
      this.priceTag = priceTag;
    }
  }
}
