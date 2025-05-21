/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.PublicKey;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

import java.util.List;

/**
 * @author eric
 */
public class NIP02 extends EventNostr {

    public NIP02(@NonNull Identity sender) {
        setSender(sender);
    }

    public NIP02 createContactListEvent(List<BaseTag> pubKeyTags) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CONTACT_LIST, pubKeyTags, "").create();
        updateEvent(genericEvent);
        return this;
    }

    /**
     * Add a pubkey tag to the contact list event
     *
     * @param tag the pubkey tag
     */
    public NIP02 addContactTag(@NonNull BaseTag tag) {
        if (!tag.getCode().equals(Constants.Tag.PUBKEY_CODE)) {
            throw new IllegalArgumentException("Tag must be a pubkey tag");
        }
        getEvent().addTag(tag);
        return this;
    }

    /**
     * Add a pubkey tag to the contact list event
     *
     * @param publicKey the public key to add to the contact list
     */
    public NIP02 addContactTag(@NonNull PublicKey publicKey) {
        return addContactTag(NIP01.createPubKeyTag(publicKey));
    }
}