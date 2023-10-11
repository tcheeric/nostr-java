package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.NIP04;
import nostr.api.NIP46.NIP46Request;
import nostr.api.NIP46.NIP46Response;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.impl.NostrConnectEvent;
import nostr.id.IIdentity;
import nostr.id.Identity;

import java.util.logging.Level;

public class NIP46 {

    @Data
    @Log
    @EqualsAndHashCode(callSuper = false)
    public static class NostrConnectEventFactory extends EventFactory<NostrConnectEvent> {

        private PublicKey recipient;

        public NostrConnectEventFactory(@NonNull NIP46Request request, @NonNull IIdentity sender, @NonNull PublicKey recipient) {
            super(sender, NIP04.encrypt(sender, request.toString(), recipient));
            this.recipient = recipient;
            var senderPk = getIdentity().getPublicKey();
            log.log(Level.INFO, "NostrConnectEventFactory Sender: {0} - Request: {1}", new Object[]{senderPk, request});
        }

        public NostrConnectEventFactory(@NonNull NIP46Response response, @NonNull IIdentity sender, @NonNull PublicKey recipient) {
            super(sender, NIP04.encrypt(sender, response.toString(), recipient));
            this.recipient = recipient;
            var senderPk = getIdentity().getPublicKey();
            log.log(Level.INFO, "NostrConnectEventFactory Sender: {0} - Response: {1}", new Object[]{senderPk, response});
        }

        public NostrConnectEvent create() {
            return new NostrConnectEvent(getIdentity().getPublicKey(), getContent(), getRecipient());
        }
    }
}
