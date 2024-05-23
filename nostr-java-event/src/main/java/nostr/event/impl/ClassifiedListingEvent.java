package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {

  public ClassifiedListingEvent(PublicKey sender, List<BaseTag> baseTags, String content) {
    this(sender, Kind.CLASSIFIED_LISTING, baseTags, content);
  }

  public ClassifiedListingEvent(PublicKey sender, Kind kind, List<BaseTag> baseTags, String content) {
    super(sender, kind, baseTags, content);
  }
}
