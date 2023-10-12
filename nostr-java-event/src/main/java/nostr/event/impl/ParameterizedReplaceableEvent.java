package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Parameterized Replaceable Events", nip = 33)
public class ParameterizedReplaceableEvent extends ReplaceableEvent {

    protected ParameterizedReplaceableEvent() {
        super();
    }

    public ParameterizedReplaceableEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
        super(sender, kind, tags, content, 30_000, 40_000);
    }
}
