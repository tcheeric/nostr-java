package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;

import java.util.List;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Ephemeral Events")
@NoArgsConstructor
public class EphemeralEvent extends NIP01Event {

    public EphemeralEvent(PublicKey pubKey, Integer kind, List<BaseTag> tags, String content) {
        super(pubKey, kind, tags, content);
    }

    public EphemeralEvent(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
        super(pubKey, kind, tags, content);
    }

    @Override
    public void validateKind() {
    	var n = getKind();
        if (20_000 <= n && n < 30_000)
            return;

        throw new AssertionError("Invalid kind value. Must be between 20000 and 30000.", null);
    }
}
