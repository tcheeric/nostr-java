package nostr.event;

import java.util.List;

import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

/**
 * @author guilhermegps
 */
@NoArgsConstructor
@Deprecated(since = "NIP-27")
public abstract class NIP08Event extends GenericEvent {

	public NIP08Event(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
		super(pubKey, kind, tags, content);
	}

}
