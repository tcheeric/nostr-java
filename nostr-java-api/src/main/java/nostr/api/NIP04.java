/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class NIP04 extends Nostr {

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

    public static void encrypt(@NonNull DirectMessageEvent dm) throws NostrException {
        var identity = Identity.getInstance();
        identity.encryptDirectMessage(dm);
    }

    public static String decrypt(@NonNull DirectMessageEvent dm) throws NostrException {
        var identity = Identity.getInstance();
        var recipient = dm.getTags()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase("p"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));
        var rcpt = (PubKeyTag) recipient;
        return identity.decryptDirectMessage(dm.getContent(), rcpt.getPublicKey());

    }

    public static class Kinds {

        public static final Integer KIND_ENCRYPTED_DIRECT_MESSAGE = 4;
    }
}
