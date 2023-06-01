package nostr.event.impl;

import nostr.base.PublicKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@Event(name = "Replaceable Events", nip = 16)
public class ReplaceableEvent extends GenericEvent {

    public ReplaceableEvent(PublicKey sender, Integer kind, TagList tags, String content) {
        super(sender, kind, tags, content);
    }

    @Override
    protected void validate() {
        if (this.getKind() >= 10_000 && this.getKind() < 20_000) {
            return;
        }
        throw new AssertionError("Invalid kind value. Must be between 10000 and 20000 (excl)", null);
    }
}
