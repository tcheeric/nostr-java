package nostr.event;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;

import java.util.List;

/**
 * @author guilhermegps
 */
@NoArgsConstructor
public abstract class NIP04Event extends GenericEvent {

	public NIP04Event(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> tags, @NonNull String content) {
		super(pubKey, kind, tags, content);
	}

	public NIP04Event(@NonNull PublicKey pubKey, @NonNull Kind kind) {
		super(pubKey, kind);
	}

}
