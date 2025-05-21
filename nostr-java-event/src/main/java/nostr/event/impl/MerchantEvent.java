package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.entities.NIP15Content;
import nostr.event.tag.GenericTag;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public abstract class MerchantEvent<T extends NIP15Content.MerchantContent> extends AddressableEvent {

    public MerchantEvent(PublicKey sender, Kind kind, List<BaseTag> tags, String content) {
        this(sender, kind.getValue(), tags, content);
    }

    public MerchantEvent(PublicKey sender, Integer kind, List<BaseTag> tags, String content) {
        super(sender, kind, tags, content);
    }

    protected abstract T getEntity();

    @Override
    protected void validateTags() {
        super.validateTags();

        // Check 'd' tag
        BaseTag dTag = getTag("d");
        if (dTag == null) {
            throw new AssertionError("Missing `d` tag.");
        }

        String id = ((GenericTag) dTag).getAttributes().getFirst().getValue().toString();
        String entityId = getEntity().getId();
        if (id != entityId) {
            throw new AssertionError("The d-tag value MUST be the same as the stall id.");
        }
    }
}
