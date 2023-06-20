
package nostr.event.impl;

import java.util.List;
import nostr.event.Kind;
import nostr.base.PublicKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Event Deletion", nip = 9)
public class DeletionEvent extends GenericEvent {

    public DeletionEvent(PublicKey pubKey, List<? extends BaseTag> tags, String content) {        
        super(pubKey, Kind.DELETION, tags, content);        
    }

    public DeletionEvent(PublicKey pubKey, List<? extends BaseTag> tags) {        
        this(pubKey, tags, "Deletion request");
    }
}
