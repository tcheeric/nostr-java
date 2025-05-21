package nostr.event.impl;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "Nostr Connect", nip = 46)
@NoArgsConstructor
public class NostrConnectRequestEvent extends AbstractBaseNostrConnectEvent {

    public NostrConnectRequestEvent(PublicKey pubKey, List<BaseTag> baseTagList, String content) {
        super(pubKey, baseTagList, content);
    }

    public PublicKey getSigner() {
        return getActor();
    }
}
