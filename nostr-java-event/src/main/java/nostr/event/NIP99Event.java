package nostr.event;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class NIP99Event extends GenericEvent {
  public NIP99Event(PublicKey pubKey, Kind kind, List<BaseTag> baseTags) {
    this(pubKey, kind, baseTags, null);
  }

  public NIP99Event(PublicKey pubKey, Kind kind, List<BaseTag> baseTags, String content) {
    super(pubKey, kind, baseTags, content);
  }
}
