package nostr.event.impl;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.list.TagList;

/**
 * @author guilhermegps
 *
 */
@Event(name = "Create Channel", nip = 28)
public class ChannelCreateEvent extends GenericEvent {

	public ChannelCreateEvent(@NonNull PublicKey pubKey, @NonNull TagList tags, String content) {
		super(pubKey, Kind.CHANNEL_CREATE, tags, content);
	}

}
