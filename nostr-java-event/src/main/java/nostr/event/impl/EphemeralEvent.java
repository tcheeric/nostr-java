package nostr.event.impl;

import java.util.ArrayList;
import java.util.List;
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
@EqualsAndHashCode(callSuper = false)
@Event(name = "Ephemeral Events", nip = 16)
public class EphemeralEvent extends GenericEvent {

    public EphemeralEvent(PublicKey pubKey, Integer kind, List<BaseTag> tags, String content) {
        super(pubKey, kind, tags, content);
    }

    public EphemeralEvent(PublicKey pubKey, Integer kind, List<BaseTag> tags) {
        this(pubKey, kind, tags, "...");
    }

    public EphemeralEvent(PublicKey sender, Integer kind, PublicKey recipient) {
        this(sender, kind, new ArrayList<>());
        this.addTag(PubKeyTag.builder().publicKey(recipient).build());
    }
    
    // TODO - Validate the kind.
}
