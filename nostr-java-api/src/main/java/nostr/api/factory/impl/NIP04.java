/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.DirectMessageEvent;

/**
 *
 * @author eric
 */
public class NIP04 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DirectMessageEventFactory extends EventFactory<DirectMessageEvent> {

        private final PublicKey recipient;

        public DirectMessageEventFactory(PublicKey recipient, String content) {
            super(content);
            this.recipient = recipient;
        }

        public DirectMessageEventFactory(List<BaseTag> tags, PublicKey recipient, String content) {
            super(content);
            this.recipient = recipient;
        }

        @Deprecated
        public DirectMessageEventFactory(PublicKey sender, PublicKey recipient, String content) {
            super(sender, content);
            this.recipient = recipient;
        }

        @Override
        public DirectMessageEvent create() {
            return new DirectMessageEvent(getSender(), recipient, getContent());
        }
    }

    public static class Kinds {
        public static final Integer KIND_ENCRYPTED_DIRECT_MESSAGE = 4;
    }

}
