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
@Event(name = "Hide Message on Channel", nip = 28)
public class HideMessageEvent extends GenericEvent {

	public HideMessageEvent(@NonNull PublicKey pubKey, @NonNull TagList tags, String content) {
		super(pubKey, Kind.HIDE_MESSAGE, tags, content);
	}

}
