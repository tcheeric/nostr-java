package nostr.api.factory.impl;

import java.util.List;

import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

public class NIP60Impl {

    public static class WalletEventFactory extends EventFactory<GenericEvent> {

        public WalletEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public GenericEvent create() {
            return new GenericEvent(getIdentity().getPublicKey(), 37375, getTags(), getContent());
        }
    }

    public static class TokenEventFactory extends EventFactory<GenericEvent> {

        public TokenEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public GenericEvent create() {
            return new GenericEvent(getIdentity().getPublicKey(), 7375, getTags(), getContent());
        }
    }

    public static class SpendingHistoryEventFactory extends EventFactory<GenericEvent> {

        public SpendingHistoryEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public GenericEvent create() {
            return new GenericEvent(getIdentity().getPublicKey(), 7376, getTags(), getContent());
        }
    }

    public static class RedemptionQuoteEventFactory extends EventFactory<GenericEvent> {

        public RedemptionQuoteEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public GenericEvent create() {
            return new GenericEvent(getIdentity().getPublicKey(), 7374, getTags(), getContent());
        }
    }

}
