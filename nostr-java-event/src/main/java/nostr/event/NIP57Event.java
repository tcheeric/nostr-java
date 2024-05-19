package nostr.event;

import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.List;

public abstract class NIP57Event extends GenericEvent {
  protected NIP57Event(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags, content);
  }
}
