/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

/**
 * @author eric
 */
public class NIP02<T extends GenericEvent> extends EventNostr<T> {

    public NIP02(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Add a pubkey tag to the contact list event
     *
     * @param tag the pubkey tag
     */
    public NIP02<T> addContactTag(@NonNull PubKeyTag tag) {
        getEvent().addTag(tag);
        return this;
    }

    /**
     * Add a pubkey tag to the contact list event
     *
     * @param publicKey the public key to add to the contact list
     */
    public NIP02<T> addContactTag(@NonNull PublicKey publicKey) {
        return addContactTag(NIP01.createPubKeyTag(publicKey));
    }
}