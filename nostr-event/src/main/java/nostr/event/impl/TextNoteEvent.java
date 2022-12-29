
package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.list.TagList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import nostr.base.annotation.Event;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Event(name = "Text Note")
public class TextNoteEvent extends GenericEvent {

    public TextNoteEvent(PublicKey pubKey, TagList tags, String content) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
    }   
}
