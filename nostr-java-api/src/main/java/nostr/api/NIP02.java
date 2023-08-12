/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.impl.NIP02.ContactListEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.ContactListEvent;

/**
 *
 * @author eric
 */
public class NIP02 extends Nostr {

    public static ContactListEvent createContactListEvent(List<BaseTag> tags) {
        return new ContactListEventFactory(tags, null).create();
    }

    public static ContactListEvent createContactListEvent(List<BaseTag> tags, String content) {
        return new ContactListEventFactory(tags, content).create();
    }    
}
