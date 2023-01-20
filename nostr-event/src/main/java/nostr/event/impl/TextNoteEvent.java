
package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.list.TagList;
import nostr.base.annotation.Event;

/**
 *
 * @author squirrel
 */
@Event(name = "Text Note")
public class TextNoteEvent extends GenericEvent {

    public TextNoteEvent(PublicKey pubKey, TagList tags, String content) {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
    }   
}
