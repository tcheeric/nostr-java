/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.ContactListEvent;
import nostr.id.Identity;

import java.util.List;

/**
 *
 * @author eric
 */
public class NIP02Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ContactListEventFactory extends EventFactory<ContactListEvent> {

        public ContactListEventFactory(@NonNull Identity sender, @NonNull String content) {
            super(sender, content);
        }

        public ContactListEventFactory(@NonNull Identity sender, @NonNull List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public ContactListEvent create() {
            return new ContactListEvent(getSender(), getTags());
        }
    }

    public static class Kinds {

        public static final Integer KIND_CONTACT_LIST = 3;
    }

}
