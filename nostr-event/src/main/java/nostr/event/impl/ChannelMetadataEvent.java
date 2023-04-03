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
@Event(name = "Channel Metadata", nip = 28)
public class ChannelMetadataEvent extends GenericEvent {

	public ChannelMetadataEvent(@NonNull PublicKey pubKey, @NonNull TagList tags, String content) {
		super(pubKey, Kind.CHANNEL_METADATA, tags, content);
	}

}
