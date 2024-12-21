package nostr.api.factory.impl;

import java.util.List;

import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

public class NIP61Impl {

    public static class NutzapInformationalEventFactory extends EventFactory<GenericEvent> {

        public NutzapInformationalEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public GenericEvent create() {
            return new GenericEvent(getIdentity().getPublicKey(), 10019, getTags(), getContent());
        }
    }

    public static class NutzapEventFactory extends EventFactory<GenericEvent> {

        public NutzapEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public GenericEvent create() {
            return new GenericEvent(getIdentity().getPublicKey(), 9321, getTags(), getContent());
        }
    }
}
