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
@Event(name = "Replaceable Events", nip = 16)
public class ReplaceableEvent extends GenericEvent {

    private final GenericEvent original;

    public ReplaceableEvent(PublicKey pubKey, TagList tags, String content, GenericEvent original) {
        super(pubKey, Kind.DELETION, tags, content);
        this.original = original;
    }

}
