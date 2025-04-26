package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GenericEventFactory extends EventFactory {

    private Integer kind;

    public GenericEventFactory(@NonNull Integer kind) {
        super();
        this.kind = kind;
    }

    public GenericEventFactory(Identity sender, @NonNull Integer kind) {
        super(sender);
        this.kind = kind;
    }

    public GenericEventFactory(@NonNull Integer kind, @NonNull String content) {
        super(null, content);
        this.kind = kind;
    }

    public GenericEventFactory(Identity sender, @NonNull Integer kind, @NonNull String content) {
        super(sender, content);
        this.kind = kind;
    }

    public GenericEventFactory(Identity sender, @NonNull Integer kind, List<BaseTag> tags, @NonNull String content) {
        super(sender, tags, content);
        this.kind = kind;
    }

    public GenericEvent create() {
        Identity identity = getIdentity();
        List<BaseTag> tags = getTags();
        String content = getContent();

        GenericEvent event = new GenericEvent(identity.getPublicKey(), kind, tags, content);

        return event;
    }

}