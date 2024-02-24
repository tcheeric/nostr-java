package nostr.event;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

/**
 * @author guilhermegps
 */
@NoArgsConstructor
public abstract class NIP01Event extends GenericEvent {

	public NIP01Event(PublicKey pubKey, Kind kind, ArrayList<BaseTag> tags) {
		super(pubKey, kind, tags);
	}

	public NIP01Event(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
		super(pubKey, kind, tags, content);
	}

}
