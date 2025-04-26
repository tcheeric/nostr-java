
package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.event.NIP01Event;
import nostr.event.tag.PubKeyTag;

import java.util.List;

/**
 * @author squirrel
 */
@Event(name = "Text Note")
@NoArgsConstructor
public class TextNoteEvent extends NIP01Event {

    public TextNoteEvent(@NonNull PublicKey pubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
    }

    public List<PubKeyTag> getRecipientPubkeyTags() {
        return this.getTags().stream()
                .filter(tag -> tag instanceof PubKeyTag)
                .map(tag -> (PubKeyTag) tag)
                .toList();
    }

    public List<PublicKey> getRecipients() {
        return this.getTags().stream()
                .filter(tag -> tag instanceof PubKeyTag)
                .map(tag -> (PubKeyTag) tag)
                .map(PubKeyTag::getPublicKey)
                .toList();
    }
}
