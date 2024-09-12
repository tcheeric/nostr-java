package nostr.event.impl;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;
import nostr.event.tag.PriceTag;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
@NoArgsConstructor
public class ClassifiedListingEvent extends NIP99Event {
  private ClassifiedListing classifiedListing;

  public ClassifiedListingEvent(@NonNull PublicKey sender, @NonNull Kind kind, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    super(sender, kind, baseTags, content);
    this.classifiedListing = classifiedListing;
    mapCustomTags();
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull String title, @NonNull String summary, @NonNull PriceTag priceTag) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, ClassifiedListing.builder(title, summary, priceTag).build());
  }

  public ClassifiedListingEvent(@NonNull PublicKey sender, List<BaseTag> baseTags, String content, @NonNull String title, @NonNull String summary, @NonNull BigDecimal number, @NonNull String currency, @NonNull String frequency) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, ClassifiedListing.builder(title, summary, new PriceTag(number, currency, frequency)).build());
  }

  @Override
  protected void validate() {
    var n = getKind();
    if (30402 <= n && n <= 30403)
      return;

    throw new AssertionError(String.format("Invalid kind value [%s]. Classified Listing must be either 30402 or 30403", n), null);
  }

  private void mapCustomTags() {
    addGenericTag("title", getNip(), classifiedListing.getTitle());
    addGenericTag("summary", getNip(), classifiedListing.getSummary());
    addGenericTag("published_at", getNip(), classifiedListing.getPublishedAt());
    addGenericTag("location", getNip(), classifiedListing.getLocation());
    addStandardTag(classifiedListing.getPriceTag());
  }
}
