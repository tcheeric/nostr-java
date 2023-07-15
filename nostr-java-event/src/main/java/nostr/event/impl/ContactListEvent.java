package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Contact List and Petnames", nip = 2)
public class ContactListEvent extends GenericEvent {

    public ContactListEvent(@NonNull PublicKey pubKey, @NonNull List<PubKeyTag> tags) {
        super(pubKey, Kind.CONTACT_LIST, tags);
    }

}
