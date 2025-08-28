package nostr.event;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.AddressableEvent;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class NIP52Event extends AddressableEvent {

  public NIP52Event(
      @NonNull PublicKey pubKey,
      @NonNull Kind kind,
      @NonNull List<BaseTag> baseTags,
      @NonNull String content) {
    super(pubKey, kind.getValue(), baseTags, content);
  }
}
