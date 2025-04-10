package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP09Impl.DeletionEventFactory;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.Deleteable;
import nostr.event.NIP09Event;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP09<T extends NIP09Event> extends EventNostr<T> {

    public NIP09(@NonNull Identity sender) {
        setSender(sender);
    }

    public NIP09<T> createDeletionEvent(@NonNull Deleteable deleteable) {
        return this.createDeletionEvent(List.of(deleteable));
    }

    /**
     * Create a NIP09 Deletion Event
     *
     * @param deleteables list of event or address tags to be deleted
     * @return the deletion event
     */
    public NIP09<T> createDeletionEvent(@NonNull List<Deleteable> deleteables) {
        List<BaseTag> tags = new ArrayList<>();

        // Handle GenericEvents
        deleteables.stream()
                .filter(d -> d instanceof GenericEvent)
                .map(d -> (GenericEvent) d)
                .forEach(event -> tags.add(new EventTag(event.getId())));

        // Handle AddressTags
        deleteables.stream()
                .filter(d -> d instanceof GenericEvent)
                .map(d -> (GenericEvent) d)
                .map(GenericEvent::getTags)
                .forEach(t -> t.stream()
                        .filter(tag -> "a".equals(tag.getCode()))
                        .forEach(tag -> {
                            if (tag instanceof GenericTag) {
                                AddressTag addressTag = toAddressTag((GenericTag) tag);
                                tags.add(addressTag);
                                tags.add(NIP25.createKindTag(addressTag.getKind()));
                            } else if (tag instanceof AddressTag) {
                                tags.add(tag);
                                tags.add(NIP25.createKindTag(((AddressTag) tag).getKind()));
                            } else {
                                throw new IllegalArgumentException("Unsupported tag type: " + tag.getClass());
                            }
                        }));

        // Add kind tags for all deleteables
        deleteables.forEach(d -> tags.add(NIP25.createKindTag(d.getKind())));

        var event = new DeletionEventFactory(getSender(), tags).create();
        this.setEvent((T) event);
        return this;
    }

    private AddressTag toAddressTag(@NonNull GenericTag genericTag) {
        IdentifierTag identifierTag = new IdentifierTag();
        identifierTag.setUuid(genericTag.getAttributes().get(1).getValue().toString());

        AddressTag addressTag = new AddressTag();
        addressTag.setIdentifierTag(identifierTag);

        String value = genericTag.getAttributes().get(0).getValue().toString();
        String[] parts = value.split(":");
        addressTag.setKind(Integer.decode(parts[0]));
        addressTag.setPublicKey(new PublicKey(parts[1]));
        if (parts.length == 3) {
            addressTag.setRelay(new Relay(parts[2]));
        }

        addressTag.setParent(genericTag.getParent());

        return addressTag;
    }
}
