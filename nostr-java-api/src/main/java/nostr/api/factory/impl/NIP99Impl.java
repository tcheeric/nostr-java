package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.ClassifiedListingEvent.ClassifiedListing;
import nostr.event.tag.IdentifierTag;
import nostr.id.IIdentity;

import java.util.ArrayList;
import java.util.List;

public class NIP99Impl {
  public static final int NIP99 = 99;

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ClassifiedListingEventFactory extends EventFactory<ClassifiedListingEvent> {
    private final ClassifiedListing classifiedListing;
    private final List<BaseTag> baseTags;

    public ClassifiedListingEventFactory(@NonNull IIdentity sender, @NonNull List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
      super(sender, content);
      this.classifiedListing = classifiedListing;
      this.baseTags = baseTags;
    }

    @Override
    public ClassifiedListingEvent create() {
      var event = new ClassifiedListingEvent(getSender(), getBaseTags(), getContent(), classifiedListing);
      event.addTag(new IdentifierTag(classifiedListing.getId()));
      return event;
    }
  }

  public static class Kinds {
    public static final Integer CLASSIFIED_LISTING = 30_402;
  }
}
