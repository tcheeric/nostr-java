package nostr.event.impl;

import lombok.EqualsAndHashCode;
import nostr.base.annotation.Event;

/**
 * @author eric
 */
@EqualsAndHashCode(callSuper = false)
@Event(name = "Parameterized Replaceable Events", nip = 33)
public class ParameterizedReplaceableEventNick extends EventDecorator {

  public ParameterizedReplaceableEventNick(GenericEventNick genericEvent) {
    super(genericEvent);
  }
}
