package nostr.event;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class NIP99Event extends GenericEvent {
  public NIP99Event(@NonNull PublicKey pubKey, Kind kind, List<BaseTag> baseTags) {
    this(pubKey, kind, baseTags, null);
  }

  public NIP99Event(@NonNull PublicKey pubKey, Kind kind, List<BaseTag> baseTags, String content) {
    super(pubKey, kind, baseTags, content);
  }
}
