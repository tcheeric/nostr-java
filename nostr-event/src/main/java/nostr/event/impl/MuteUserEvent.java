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
@Event(name = "Mute User on Channel", nip = 28)
public class MuteUserEvent extends GenericEvent {

	public MuteUserEvent(@NonNull PublicKey pubKey, @NonNull TagList tags, String content) {
		super(pubKey, Kind.MUTE_USER, tags, content);
	}

}
