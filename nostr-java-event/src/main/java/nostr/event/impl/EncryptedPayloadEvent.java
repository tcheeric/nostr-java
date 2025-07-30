package nostr.event.impl;

import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;

@Event(name = "Encrypted Payload (Versioned)", nip = 44)
@Deprecated(forRemoval = true, since = "0.6.6-SNAPSHOT")
@NoArgsConstructor
public class EncryptedPayloadEvent extends GenericEvent {

    public EncryptedPayloadEvent(PublicKey sender, List<BaseTag> tags, String content) {
        super(sender, Kind.ENCRYPTED_PAYLOADS, tags, content);
    }

    public EncryptedPayloadEvent(PublicKey sender, PublicKey recipient, String content) {
        super(sender, Kind.ENCRYPTED_PAYLOADS);
        this.setContent(content);
        this.addTag(PubKeyTag.builder().publicKey(recipient).build());
    }

    @Override
    protected void validateKind() {
        if (getKind() != Kind.ENCRYPTED_PAYLOADS.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.ENCRYPTED_PAYLOADS.getValue());
        }
    }
}
