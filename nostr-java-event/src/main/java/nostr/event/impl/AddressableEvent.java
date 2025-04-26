package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Addressable Events")
@NoArgsConstructor
public class AddressableEvent extends NIP01Event {

    public AddressableEvent(PublicKey pubKey, Integer kind, List<BaseTag> tags, String content) {
        super(pubKey, kind, tags, content);
    }

    @Override
    public void validateKind() {
        super.validateKind();

        var n = getKind();
        if (30_000 <= n && n < 40_000)
            return;

        throw new AssertionError("Invalid kind value. Must be between 30000 and 40000.", null);
    }
}
