/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import nostr.base.PublicKey;
import nostr.event.impl.DirectMessageEvent;

/**
 *
 * @author eric
 */
public class NIP04 {

    public static class DirectMessageEventFactory extends EventFactory<DirectMessageEvent> {

        private final PublicKey recipient;
        
        public DirectMessageEventFactory(PublicKey sender, PublicKey recipient, String content) {
            super(sender, content);
            this.recipient = recipient;
        }

        @Override
        public DirectMessageEvent create() {
            return new DirectMessageEvent(getSender(), recipient, getContent());
        }

    }
}
