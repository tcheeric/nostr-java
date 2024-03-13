/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP40Impl.ExpirationTagFactory;
import nostr.event.tag.ExpirationTag;

/**
 *
 * @author eric
 */
public class NIP40 {

    /**
     * 
     * @param expiration
     */
    public static ExpirationTag createExpirationTag(@NonNull Integer expiration) {
        return new ExpirationTagFactory(expiration).create();
    }    
}
