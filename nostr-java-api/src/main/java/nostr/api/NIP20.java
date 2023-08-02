/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.api.factory.MessageFactory;
import nostr.base.IEvent;
import nostr.event.message.OkMessage;

/**
 *
 * @author eric
 */
public class NIP20 extends Nostr {
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OkMessageFactory extends MessageFactory<OkMessage> {

        private final IEvent event;
        private final boolean flag;
        private final String message;

        public OkMessageFactory(IEvent event, boolean flag, String message) {
            this.event = event;
            this.flag = flag;
            this.message = message;
        }
        
        
        @Override
        public OkMessage create() {
            return new OkMessage(event.getId(), flag, message);
        }        
    }
    
    
}
