package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP99Impl.ClassifiedListingEventFactory;
import nostr.event.NIP99Event;
import nostr.event.impl.ClassifiedListingEvent.ClassifiedListing;
import nostr.event.tag.PriceTag;
import nostr.id.IIdentity;

public class NIP99<T extends NIP99Event> extends EventNostr<T> {
  public NIP99(@NonNull IIdentity sender) {
    setSender(sender);
  }

  public NIP99<T> createClassifiedListingEvent(@NonNull ClassifiedListing classifiedListing, @NonNull PriceTag priceTag) {
    var event = new ClassifiedListingEventFactory(getSender(), classifiedListing, priceTag).create();
    setEvent((T) event);
    return this;
  }
}
