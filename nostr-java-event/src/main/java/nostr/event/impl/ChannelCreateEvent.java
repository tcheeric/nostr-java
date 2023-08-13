package nostr.event.impl;

import java.util.ArrayList;
import lombok.NonNull;
import nostr.base.ChannelProfile;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import static nostr.event.impl.GenericEvent.escapeJsonString;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Create Channel", nip = 28)
public class ChannelCreateEvent extends GenericEvent {

    public ChannelCreateEvent(@NonNull PublicKey pubKey, ChannelProfile profile) {
        super(pubKey, Kind.CHANNEL_CREATE, new ArrayList<>(), escapeJsonString(profile.toString()));
    }
}
