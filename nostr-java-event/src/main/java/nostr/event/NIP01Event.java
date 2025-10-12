package nostr.event;

import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.List;

/**
 * @author guilhermegps
 */
@NoArgsConstructor
public abstract class NIP01Event extends GenericEvent {

  public NIP01Event(PublicKey pubKey, Kind kind, List<BaseTag> tags) {
    super(pubKey, kind, tags);
  }

  public NIP01Event(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags, content);
  }

  public NIP01Event(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
    super(sender, kind, tags, content);
  }
}
