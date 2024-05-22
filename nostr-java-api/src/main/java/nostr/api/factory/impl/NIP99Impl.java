package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.ClassifiedListingTag;
import nostr.id.Identity;

import java.util.List;

public class NIP99Impl {

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ClassifiedListingEventFactory extends EventFactory<ClassifiedListingEvent> {
    private final ClassifiedListingTag classifiedListingTag;
    private final Kind kind;

    public ClassifiedListingEventFactory(Identity sender, List<BaseTag> baseTags, String content, ClassifiedListingTag classifiedListingTag) {
      this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListingTag);
    }

    public ClassifiedListingEventFactory(Identity sender, Kind kind, List<BaseTag> baseTags, String content, ClassifiedListingTag classifiedListingTag) {
      super(sender, baseTags, content);
      this.kind = kind;
      this.classifiedListingTag = classifiedListingTag;
    }

    @Override
    public ClassifiedListingEvent create() {
      return new ClassifiedListingEvent(getSender(), getKind(), getTags(), getContent(), classifiedListingTag);
    }
  }
}
