/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericTagFactory;
import nostr.config.Constants;
import nostr.event.tag.GenericTag;

/**
 *
 * @author eric
 */
public class NIP40 {

    /**
     * 
     * @param expiration
     */
    public static GenericTag createExpirationTag(@NonNull Integer expiration) {
        return new GenericTagFactory(Constants.Tag.EXPIRATION_CODE, expiration.toString()).create();
    }    
}
