package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP99Impl.ClassifiedListingEventFactory;
import nostr.event.BaseTag;
import nostr.event.NIP99Event;
import nostr.event.impl.ClassifiedListing;
import nostr.id.Identity;

import java.util.List;

public class NIP99<T extends NIP99Event> extends EventNostr<T> {
  public NIP99(@NonNull Identity sender) {
    setSender(sender);
  }

  public NIP99<T> createClassifiedListingEvent(@NonNull List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    setEvent((T) new ClassifiedListingEventFactory(getSender(), baseTags, content, classifiedListing).create());
    return this;
  }
}
