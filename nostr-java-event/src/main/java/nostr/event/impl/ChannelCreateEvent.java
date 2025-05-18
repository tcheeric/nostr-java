package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.entities.ChannelProfile;

import java.util.ArrayList;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Create Channel", nip = 28)
@NoArgsConstructor
public class ChannelCreateEvent extends GenericEvent {

    public ChannelCreateEvent(PublicKey pubKey, String content) {
        super(pubKey, Kind.CHANNEL_CREATE, new ArrayList<>(), content);
    }

    @SneakyThrows
    public ChannelProfile getChannelProfile() {
        String content = getContent();
        return MAPPER_AFTERBURNER.readValue(content, ChannelProfile.class);
    }

}
