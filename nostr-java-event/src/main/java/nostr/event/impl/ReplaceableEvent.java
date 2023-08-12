package nostr.event.impl;

import java.util.List;
import nostr.base.PublicKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@Event(name = "Replaceable Events", nip = 16)
public class ReplaceableEvent extends GenericEvent {

    private final int minKind;
    private final int maxKind;
          
    public ReplaceableEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
        this(sender, kind, tags, content, 10_000, 20_000);
    }

    protected ReplaceableEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content, int minKind, int maxKind) {
        super(sender, kind, tags, content);
        this.minKind = minKind;
        this.maxKind = maxKind;
    }

    @Override
    protected void validate() {
        if (this.getKind() >= this.minKind && this.getKind() < this.maxKind) {
            return;
        }
        throw new AssertionError(String.format("Invalid kind value. Must be between %d and %d (excl)", this.minKind, this.maxKind), null);
    }
}
