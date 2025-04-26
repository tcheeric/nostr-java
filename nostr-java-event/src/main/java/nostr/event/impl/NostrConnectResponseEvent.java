package nostr.event.impl;

import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

import java.util.List;

@Event(name = "Nostr Connect", nip = 46)
@NoArgsConstructor
public class NostrConnectResponseEvent extends AbstractBaseNostrConnectEvent {

    public NostrConnectResponseEvent(PublicKey pubKey, List<BaseTag> baseTagList, String content) {
        super(pubKey, baseTagList, content);
    }

    public PublicKey getApp() {
        return getActor();
    }

}
