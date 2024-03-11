package nostr.event;

import java.util.List;

import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

/**
 * @author guilhermegps
 */
@NoArgsConstructor
@Deprecated(since = "NIP-44")
public abstract class NIP04Event extends GenericEvent {

	public NIP04Event(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
		super(pubKey, kind, tags, content);
	}

	public NIP04Event(PublicKey pubKey, Kind kind) {
		super(pubKey, kind);
	}

}
