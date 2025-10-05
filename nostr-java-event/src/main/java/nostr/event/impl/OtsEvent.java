package nostr.event.impl;

import java.util.List;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

/**
 * @author squirrel
 */
@Event(name = "OpenTimestamps Attestations for Events")
@NoArgsConstructor
public class OtsEvent extends GenericEvent {

  public OtsEvent(@NonNull PublicKey pubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
    super(pubKey, Kind.OTS_EVENT, tags, content);
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.OTS_EVENT.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.OTS_EVENT.getValue());
    }
  }
}
