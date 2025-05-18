/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

/**
 *
 * @author eric
 */
public class NIP40 {

    /**
     * 
     * @param expiration
     */
    public static BaseTag createExpirationTag(@NonNull Integer expiration) {
        return new BaseTagFactory(Constants.Tag.EXPIRATION_CODE, expiration.toString()).create();
    }    
}
