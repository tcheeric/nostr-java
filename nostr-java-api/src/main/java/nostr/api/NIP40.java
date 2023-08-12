/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import nostr.api.factory.impl.NIP40.ExpirationTagFactory;
import nostr.event.tag.ExpirationTag;

/**
 *
 * @author eric
 */
public class NIP40 extends Nostr {

    public static ExpirationTag createExpirationTag(Integer expiration) {
        return new ExpirationTagFactory(expiration).create();
    }    
}
