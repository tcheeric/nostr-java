package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.ClassifiedListingEvent.ClassifiedListing;
import nostr.id.Identity;

import java.util.List;

public class NIP99Impl {

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ClassifiedListingEventFactory extends EventFactory<ClassifiedListingEvent> {
    private final ClassifiedListing classifiedListing;
    private final Kind kind;

    public ClassifiedListingEventFactory(@NonNull Identity sender, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
      this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
    }

    public ClassifiedListingEventFactory(@NonNull Identity sender, @NonNull Kind kind, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
      super(sender, baseTags, content);
      this.kind = kind;
      this.classifiedListing = classifiedListing;
    }

    @Override
    public ClassifiedListingEvent create() {
      return new ClassifiedListingEvent(getSender(), getKind(), getTags(), getContent(), classifiedListing);
    }
  }
}
