package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.EncryptedPayloadEvent;
import nostr.id.Identity;

import java.util.List;

public class NIP44Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EncryptedPayloadEventFactory extends EventFactory<EncryptedPayloadEvent> {

        private final PublicKey recipient;

        public EncryptedPayloadEventFactory(@NonNull Identity sender, @NonNull PublicKey recipient, @NonNull String content) {
            super(sender, content);
            this.recipient = recipient;
        }

        public EncryptedPayloadEventFactory(@NonNull Identity identity, @NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
            super(identity, content);
            this.recipient = recipient;
        }

        @Override
        public EncryptedPayloadEvent create() {
            return new EncryptedPayloadEvent(getSender(), recipient, getContent());
        }
    }

    public static class Kinds {
        public static final Integer KIND_ENCRYPTED_PAYLOAD = 44;
    }


}
