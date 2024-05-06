package nostr.event;

import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.List;

@NoArgsConstructor
public abstract class NIP99Event extends GenericEvent {

  public NIP99Event(PublicKey pubKey, Kind kind, List<BaseTag> tags) {
    super(pubKey, kind, tags);
  }

  public NIP99Event(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags, content);
  }

  public NIP99Event(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
    super(sender, kind, tags, content);
  }
}
