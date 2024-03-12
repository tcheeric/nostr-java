package nostr.event.impl;

import lombok.EqualsAndHashCode;
import nostr.base.annotation.Event;

/**
 * @author eric
 */
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
public class NostrMarketplaceEventNick extends EventDecorator {

  public NostrMarketplaceEventNick(GenericEventNick genericEvent) {
    super(genericEvent);
  }
}
