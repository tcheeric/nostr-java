package nostr.event.impl;

import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;

@NoArgsConstructor
public abstract class AbstractBaseNostrConnectEvent extends EphemeralEvent {
    public AbstractBaseNostrConnectEvent(PublicKey pubKey, List<BaseTag> baseTagList, String content) {
        super(pubKey, 24_133, baseTagList, content);
    }

    public PublicKey getActor() {
        return ((PubKeyTag) getTag("p")).getPublicKey();
    }

    public void validate() {
        super.validate();

        // 1. p - tag validation
        getTags().stream().filter(tag -> tag instanceof PubKeyTag).findFirst()
                .orElseThrow(() -> new AssertionError("Invalid `tags`: Must include at least one valid PubKeyTag."));
    }
}
