package nostr.event.impl;

import nostr.event.tag.PubKeyTag;
import nostr.event.Kind;
import nostr.base.PublicKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.ITag;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Handling Mentions", nip = 8)
@Log
public final class MentionsEvent extends GenericEvent {

    public MentionsEvent(PublicKey pubKey, TagList tags, String content) {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        super.update();

        int index = 0;

        while (getTags().getList().iterator().hasNext()) {
            ITag tag = (ITag) getTags().getList().iterator().next();
            String replacement = "#[" + index++ + "]";
            setContent(this.getContent().replace(((PubKeyTag) tag).getStringPubKey(), replacement));
        }
    }
}
