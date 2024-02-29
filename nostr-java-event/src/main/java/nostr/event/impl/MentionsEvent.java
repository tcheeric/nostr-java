package nostr.event.impl;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.ITag;
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

        int index = 0;

        // TODO - Refactor with the EntityAttributeUtil class
        while (getTags().iterator().hasNext()) {
            ITag tag = getTags().iterator().next();
            String replacement = "#[" + index++ + "]";
            setContent(this.getContent().replace(((PubKeyTag) tag).getPublicKey().toString(), replacement));
        }
    }
}
