/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.impl.ContactListEvent;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author eric
 */
public class NIP02 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ContactListEventFactory extends EventFactory<ContactListEvent> {

        private List<PubKeyTag> relatedPubKeys;

        public ContactListEventFactory(PublicKey sender, String content) {
            super(sender, content);
            this.relatedPubKeys = new ArrayList<>();
        }

        
        @Override
        public ContactListEvent createEvent() {
            return new ContactListEvent(getSender(), relatedPubKeys);
        }

    }
}
