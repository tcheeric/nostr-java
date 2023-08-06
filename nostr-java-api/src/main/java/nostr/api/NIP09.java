/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.DeletionEvent;

/**
 *
 * @author eric
 */
public class NIP09 extends Nostr {

    public static class EventDeletionFactory extends EventFactory<DeletionEvent> {

        public EventDeletionFactory() {
            super(null);
        }

        public EventDeletionFactory(List<BaseTag> tags) {
            super(tags, null);
        }

        @Deprecated
        public EventDeletionFactory(PublicKey sender) {
            super(sender, null);
        }

        @Override
        public DeletionEvent create() {
            return new DeletionEvent(getSender(), getTags());
        }

    }

    public static class Kinds {

        public static final Integer KIND_DELETION = 5;
    }

}
