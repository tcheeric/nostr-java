package nostr.event;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class NIP52Event extends GenericEvent {

  public NIP52Event(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> baseTags, @NonNull String content) {
    super(pubKey, kind.getValue(), baseTags, content);
  }
}
