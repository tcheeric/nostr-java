package nostr.event;

import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

/**
 * @author guilhermegps
 */
@NoArgsConstructor
public abstract class NIP05Event extends GenericEvent {

  public NIP05Event(PublicKey pubKey, Kind kind) {
    super(pubKey, kind);
  }
}
