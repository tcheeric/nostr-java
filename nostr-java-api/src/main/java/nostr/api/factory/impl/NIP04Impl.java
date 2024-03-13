/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.DirectMessageEvent;
import nostr.id.IIdentity;

/**
 *
 * @author eric
 */
@Deprecated(since = "NIP-44")
public class NIP04Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DirectMessageEventFactory extends EventFactory<DirectMessageEvent> {

        private final PublicKey recipient;

        public DirectMessageEventFactory(@NonNull PublicKey recipient, @NonNull String content) {
            super(content);
            this.recipient = recipient;
        }

        public DirectMessageEventFactory(@NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
            super(content);
            this.recipient = recipient;
        }

        public DirectMessageEventFactory(@NonNull IIdentity sender, @NonNull PublicKey recipient, @NonNull String content) {
            super(sender, content);
            this.recipient = recipient;
        }

        public DirectMessageEventFactory(@NonNull IIdentity identity, @NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
            super(identity, content);
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
