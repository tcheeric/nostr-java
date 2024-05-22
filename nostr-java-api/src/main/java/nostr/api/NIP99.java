package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP99Impl.ClassifiedListingEventFactory;
import nostr.event.BaseTag;
import nostr.event.NIP99Event;
import nostr.event.tag.ClassifiedListingTag;
import nostr.id.Identity;

import java.util.List;

public class NIP99<T extends NIP99Event> extends EventNostr<T> {
  public NIP99(@NonNull Identity sender) {
    setSender(sender);
  }

  public NIP99<T> createClassifiedListingEvent(@NonNull List<BaseTag> baseTags, @NonNull String content, @NonNull ClassifiedListingTag classifiedListingTag) {
    var event = new ClassifiedListingEventFactory(getSender(), baseTags, content, classifiedListingTag).create();
    setEvent((T) event);
    return this;
  }
}
