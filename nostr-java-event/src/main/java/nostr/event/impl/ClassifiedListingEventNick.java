package nostr.event.impl;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.annotation.Event;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEventNick extends EventDecorator {
  @NonNull
  private final GenericEventNick genericEvent;
  @NonNull
  private final List<String> price;
  //  TODO: refactor below fields into GenericEventImpl for its builder
  @NonNull
  private final String content;

  @Builder
  private ClassifiedListingEventNick(GenericEventNick genericEvent, String content, List<String> price) {
    super(genericEvent);
    this.genericEvent = genericEvent;
    this.content = content;
    this.price = price;
  }
}
