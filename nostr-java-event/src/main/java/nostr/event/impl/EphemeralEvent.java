package nostr.event.impl;

import java.util.List;
import nostr.event.Kind;
import nostr.base.PublicKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@Event(name = "Ephemeral Events", nip = 16)
public class EphemeralEvent extends GenericEvent {

    public EphemeralEvent(PublicKey pubKey, List<? extends BaseTag> tags, String content) {
        super(pubKey, Kind.EPHEMEREAL_EVENT, tags, content);
    }

    public EphemeralEvent(PublicKey pubKey, List<? extends BaseTag> tags) {
        this(pubKey, tags, "...");
    }

    public EphemeralEvent(PublicKey sender, PublicKey recipient) {
        super(sender, Kind.EPHEMEREAL_EVENT);
        this.addTag(PubKeyTag.builder().publicKey(recipient).build());
    }
}
