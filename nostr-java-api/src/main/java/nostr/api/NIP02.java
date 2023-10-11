/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.NIP02.ContactListEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.ContactListEvent;
import nostr.id.IIdentity;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP02 extends Nostr {

    /**
     * Create a contact list event
     * @param tags the list of pubkey tag objects
     * @return a contact list event
     */
    public static ContactListEvent createContactListEvent(@NonNull List<BaseTag> tags) {
        return new ContactListEventFactory(tags, "").create();
    }

    public static ContactListEvent createContactListEvent(@NonNull IIdentity sender, @NonNull List<BaseTag> tags) {
        return new ContactListEventFactory(sender, tags, "").create();
    }

    /**
     * Create a contact list event
     * @param tags the list of pubkey tag objects
     * @param content the note's optional content
     * @return a contact list event
     */
    public static ContactListEvent createContactListEvent(@NonNull List<BaseTag> tags, String content) {
        return new ContactListEventFactory(tags, content).create();
    }

    public static ContactListEvent createContactListEvent(@NonNull IIdentity sender, @NonNull List<BaseTag> tags, String content) {
        return new ContactListEventFactory(sender, tags, content).create();
    }
}
