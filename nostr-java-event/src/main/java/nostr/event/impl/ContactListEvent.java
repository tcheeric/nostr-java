package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;

import java.util.List;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Contact List and Petnames", nip = 2)
@NoArgsConstructor
public class ContactListEvent extends GenericEvent {

    public ContactListEvent(@NonNull PublicKey pubKey, @NonNull List<BaseTag> tags) {
        super(pubKey, Kind.CONTACT_LIST, tags);
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        boolean hasPTag = getTags().stream().anyMatch(t -> "p".equals(t.getCode()));
        if (!hasPTag) {
            throw new AssertionError("Missing `p` tag for contact list entries.");
        }
    }
}
