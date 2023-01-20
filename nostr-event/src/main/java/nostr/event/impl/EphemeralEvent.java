package nostr.event.impl;

import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.list.TagList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@Event(name = "Ephemeral Events", nip = 16)
public class EphemeralEvent extends GenericEvent {

    public EphemeralEvent(PublicKey pubKey, TagList tags, String content) {
        super(pubKey, Kind.EPHEMEREAL_EVENT, tags, content);
    }

    public EphemeralEvent(PublicKey pubKey, TagList tags) {
        this(pubKey, tags, "...");
    }
}
