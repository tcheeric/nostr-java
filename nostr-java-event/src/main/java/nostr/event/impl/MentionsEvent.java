package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Handling Mentions", nip = 8)
@NoArgsConstructor
public final class MentionsEvent extends GenericEvent {

    public MentionsEvent(PublicKey pubKey, Integer kind, List<BaseTag> tags, String content) {
        super(pubKey, kind, tags, content);
    }

    @Override
    public void update() {
        AtomicInteger counter = new AtomicInteger(0);

        // TODO - Refactor with the EntityAttributeUtil class
        getTags().forEach(tag -> {
            String replacement = "#[" + counter.getAndIncrement() + "]";
            setContent(this.getContent().replace(((PubKeyTag) tag).getPublicKey().toString(), replacement));
        });

        super.update();
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Validate `tags` field for at least one PubKeyTag
        boolean hasValidPubKeyTag = this.getTags().stream()
                .anyMatch(tag -> tag instanceof PubKeyTag);
        if (!hasValidPubKeyTag) {
            throw new AssertionError("Invalid `tags`: Must include at least one valid PubKeyTag.");
        }
    }
}
