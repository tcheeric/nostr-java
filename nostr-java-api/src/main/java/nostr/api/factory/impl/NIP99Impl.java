package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.id.Identity;

import java.util.List;

public class NIP99Impl {

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ClassifiedListingEventFactory extends EventFactory<ClassifiedListingEvent> {
    private final ClassifiedListing classifiedListing;
    private final Kind kind;

    public ClassifiedListingEventFactory(Identity sender, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
      this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListing);
    }

    public ClassifiedListingEventFactory(Identity sender, Kind kind, List<BaseTag> baseTags, String content, ClassifiedListing classifiedListing) {
      super(sender, baseTags.stream().toList(), content);
      this.kind = kind;
      this.classifiedListing = classifiedListing;
    }

    @Override
    public ClassifiedListingEvent create() {
      return new ClassifiedListingEvent(getSender(), getKind(), getTags(), getContent(), classifiedListing);
    }
  }
}
