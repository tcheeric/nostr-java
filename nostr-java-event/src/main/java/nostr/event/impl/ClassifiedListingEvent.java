package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;
import nostr.event.tag.ClassifiedListingTag;

import java.util.List;

@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {
  public ClassifiedListingEvent(PublicKey sender, List<BaseTag> baseTags, String content, ClassifiedListingTag classifiedListingTag) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content, classifiedListingTag);
  }

  public ClassifiedListingEvent(PublicKey sender, Kind kind, List<BaseTag> baseTags, String content, ClassifiedListingTag classifiedListingTag) {
    super(sender, kind, baseTags, content);
    this.addTag(classifiedListingTag);
  }
}
