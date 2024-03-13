package nostr.event.impl;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Parameterized Replaceable Events", nip = 1)
public class ParameterizedReplaceableEvent extends NIP01Event {

    protected ParameterizedReplaceableEvent() {
    }

    public ParameterizedReplaceableEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
        super(sender, kind, tags, content);
    }

    @Override
    protected void validate() {
    	var n = getKind();
        if (30000 <= n && n < 40000)
            return;

        throw new AssertionError("Invalid kind value. Must be between 30000 and 40000", null);
    }
}
