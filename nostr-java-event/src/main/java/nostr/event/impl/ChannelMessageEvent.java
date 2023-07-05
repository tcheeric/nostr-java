package nostr.event.impl;

import java.util.List;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Channel Message", nip = 28)
public class ChannelMessageEvent extends GenericEvent {

    public ChannelMessageEvent(@NonNull PublicKey pubKey, @NonNull List<? extends BaseTag> tags, String content) {
        super(pubKey, Kind.CHANNEL_MESSAGE, tags, content);
    }
}
