package nostr.event.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP08Event;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Handling Mentions", nip = 8)
public final class MentionsEvent extends NIP08Event {

    public MentionsEvent(PublicKey pubKey, List<BaseTag> tags, String content) {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
    }

    @Override
    public void update() {
        super.update();

        AtomicInteger counter = new AtomicInteger(0);

        // TODO - Refactor with the EntityAttributeUtil class
        getTags().forEach(tag -> {
            String replacement = "#[" + counter.getAndIncrement() + "]";
            setContent(this.getContent().replace(((PubKeyTag) tag).getPublicKey().toString(), replacement));
        });
    }
}
