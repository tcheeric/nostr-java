package nostr.event.impl;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
public class CreateOrUpdateProductEvent extends NostrMarketplaceEvent {

    public CreateOrUpdateProductEvent(PublicKey sender, List<BaseTag> tags, @NonNull Product product) {
        super(sender, 30018, tags, product);
    }
}
