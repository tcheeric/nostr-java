package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.tag.PubKeyTag;

import java.util.List;

@Event(name = "Encrypted Payload (Versioned)", nip = 44)
public class EncryptedPayloadEvent extends GenericEvent {

        protected EncryptedPayloadEvent() {
        }

        public EncryptedPayloadEvent(PublicKey sender, List<BaseTag> tags, String content) {
            super(sender, Kind.ENCRYPTED_PAYLOADS, tags, content);
        }

        public EncryptedPayloadEvent(PublicKey sender, PublicKey recipient, String content) {
            super(sender, Kind.ENCRYPTED_PAYLOADS);
            this.setContent(content);
            this.addTag(PubKeyTag.builder().publicKey(recipient).build());
        }
}
