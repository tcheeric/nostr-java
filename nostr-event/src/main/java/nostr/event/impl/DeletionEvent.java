
package nostr.event.impl;

import nostr.event.Kind;
import nostr.base.PublicKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Event Deletion", nip = 9)
public class DeletionEvent extends GenericEvent {

    public DeletionEvent(PublicKey pubKey, TagList tagList, String content) {        
        super(pubKey, Kind.DELETION, tagList, content);        
    }

    public DeletionEvent(PublicKey pubKey, TagList tagList) {        
        this(pubKey, tagList, "Deletion request");
    }
}
