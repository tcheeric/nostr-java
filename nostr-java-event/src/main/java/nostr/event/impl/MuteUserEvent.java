package nostr.event.impl;

import java.util.List;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.tag.PubKeyTag;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Mute User on Channel", nip = 28)
public class MuteUserEvent extends GenericEvent {

    public MuteUserEvent(@NonNull PublicKey pubKey, @NonNull List<? extends BaseTag> tags, String content) {
        super(pubKey, Kind.MUTE_USER, tags, content);
    }

    public MuteUserEvent(@NonNull PublicKey pubKey, @NonNull PublicKey mutedUser, String content) {
        super(pubKey, Kind.MUTE_USER);
        this.addTag(PubKeyTag.builder().publicKey(mutedUser).build());
        this.setContent(content);        
    }
}
