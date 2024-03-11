/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP02Impl;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP02<T extends GenericEvent> extends EventNostr<T> {

    public NIP02(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create an empty contact list event
     *
     * @return a contact list event
     */
    public NIP02<T> createContactListEvent() {
        var sender = getSender();
        return createContactListEvent(new ArrayList<>());
    }

    /**
     * Create a contact list event
     *
     * @param tags the list of pubkey tag objects
     * @return
     */
    public NIP02<T> createContactListEvent(@NonNull List<BaseTag> tags) {
        var factory = new NIP02Impl.ContactListEventFactory(tags, "");
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * Add a pubkey tag to the contact list event
     *
     * @param tag the pubkey tag
     * @return
     */
    public NIP02<T> addContactTag(@NonNull PubKeyTag tag) {
        getEvent().addTag(tag);
        return this;
    }

    /**
     * Add a pubkey tag to the contact list event
     *
     * @param publicKey the public key to add to the contact list
     * @return
     */
    public NIP02<T> addContactTag(@NonNull PublicKey publicKey) {
        getEvent().addTag(NIP01.createPubKeyTag(publicKey));
        return this;
    }
}