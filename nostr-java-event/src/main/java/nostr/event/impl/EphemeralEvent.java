package nostr.event.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Ephemeral Events", nip = 1)
public class EphemeralEvent extends NIP01Event {

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

    @Override
    protected void validate() {
    	var n = getKind();
        if (20000 <= n && n < 30000)
            return;

        throw new AssertionError("Invalid kind value. Must be between 20000 and 30000.", null);
    }
}
