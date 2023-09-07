/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.NIP09.DeletionEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.DeletionEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;

/**
 *
 * @author eric
 */
public class NIP09 extends Nostr {

    /**
     * Create a NIP09 Deletion Event
     *
     * @param tags list of event or address tags to be deleted
     * @return the deletion event
     */
    public static DeletionEvent createDeletionEvent(@NonNull List<BaseTag> tags) {
        return new DeletionEventFactory(tags).create();
    }

    /**
     * Create a NIP09 Deletion Event
     *
     * @param event the (single) event to delete
     * @return the deletion event
     */
    public static DeletionEvent createDeletionEvent(@NonNull GenericEvent event) {
        EventTag tag = new EventTag(event.getId());
        List<BaseTag> tags = new ArrayList<>();
        tags.add(tag);

        return new DeletionEventFactory(tags).create();
    }
}
