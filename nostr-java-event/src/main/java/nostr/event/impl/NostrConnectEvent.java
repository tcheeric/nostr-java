package nostr.event.impl;

import java.util.ArrayList;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.tag.PubKeyTag;

@EqualsAndHashCode(callSuper = false)
@Event(name = "Nostr Connect", nip = 46)
public class NostrConnectEvent extends EphemeralEvent {

  public NostrConnectEvent(
      @NonNull PublicKey sender, @NonNull String content, @NonNull PublicKey recipient) {
    super(sender, 24133, new ArrayList<>(), content);
    this.addTag(PubKeyTag.builder().publicKey(recipient).build());
  }
}
