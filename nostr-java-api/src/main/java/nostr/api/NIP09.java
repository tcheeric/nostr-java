package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.Deleteable;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP09 extends EventNostr {

    public NIP09(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create a NIP09 Deletion Event
     *
     * @param deleteables an array of event or address tags to be deleted
     * @return
     */
    public NIP09 createDeletionEvent(@NonNull Deleteable... deleteables) {
        return this.createDeletionEvent(List.of(deleteables));
    }

    /**
     * Create a NIP09 Deletion Event
     *
     * @param deleteables list of event or address tags to be deleted
     * @return
     */
    public NIP09 createDeletionEvent(@NonNull List<Deleteable> deleteables) {
        List<BaseTag> tags = getTags(deleteables);
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.EVENT_DELETION, tags, "").create();
        this.updateEvent(genericEvent);

        return this;
    }

    private List<BaseTag> getTags(List<Deleteable> deleteables) {
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
                        .map(tag -> (AddressTag) tag)
                        .forEach(tag -> {
                            tags.add(tag);
                            tags.add(NIP25.createKindTag(tag.getKind()));
                        }));

        // Add kind tags for all deleteables
        deleteables.forEach(d -> tags.add(NIP25.createKindTag(d.getKind())));

        return tags;
    }
}
