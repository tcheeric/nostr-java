/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.impl.DirectMessageEvent;
import nostr.id.Identity;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class NIP04 extends Api {

    public static class DirectMessageEventFactory extends EventFactory<DirectMessageEvent> {

        private final PublicKey recipient;

        public DirectMessageEventFactory(PublicKey recipient, String content) {
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

    public static void encrypt(@NonNull DirectMessageEvent dm) throws NostrException {
        var identity = Identity.getInstance();
        identity.encryptDirectMessage(dm);
    }

    public static String decrypt(@NonNull DirectMessageEvent dm) throws NostrException {
        var identity = Identity.getInstance();
        return identity.decryptDirectMessage(dm.getContent(), dm.getPubKey());
    }

    public static class Kinds {

        public static final Integer KIND_ENCRYPTED_DIRECT_MESSAGE = 4;
    }
}
