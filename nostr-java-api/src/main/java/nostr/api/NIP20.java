/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import nostr.api.factory.impl.NIP20.OkMessageFactory;
import nostr.base.IEvent;
import nostr.event.message.OkMessage;

/**
 *
 * @author eric
 */
public class NIP20 extends Nostr {
    
    /**
     * Create an OK message providing information about if an event was accepted or rejected.
     * @param event the related event
     * @param flag 
     * @param message additional information as to why the command succeeded or failed
     * @return the OK message
     */
    public static OkMessage createOkMessage(IEvent event, boolean flag, String message) {
        return new OkMessageFactory(event, flag, message).create();
    }    
}
