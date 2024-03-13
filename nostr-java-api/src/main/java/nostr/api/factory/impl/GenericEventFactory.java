package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.impl.GenericEvent;
import nostr.id.IIdentity;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenericEventFactory extends EventFactory<GenericEvent> {

    private Integer kind;

    public GenericEventFactory(IIdentity sender, @NonNull Integer kind, @NonNull String content) {
        super(sender, content);
        this.kind = kind;
    }

    public GenericEventFactory(@NonNull Integer kind, @NonNull String content) {
        super(content);
        this.kind = kind;
    }

    public GenericEvent create() {
        return new GenericEvent(getIdentity().getPublicKey(), getKind(), getTags(), getContent());
    }

}