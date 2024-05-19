package nostr.event;

import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.List;

@NoArgsConstructor
public abstract class NIP57Event extends GenericEvent {
  public NIP57Event(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags, content);
  }
}
