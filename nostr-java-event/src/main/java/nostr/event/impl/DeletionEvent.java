
package nostr.event.impl;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP09Event;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Event Deletion", nip = 9)
public class DeletionEvent extends NIP09Event {

    public DeletionEvent(PublicKey pubKey, List<BaseTag> tags, String content) {        
        super(pubKey, Kind.DELETION, tags, content);        
    }

    public DeletionEvent(PublicKey pubKey, List<BaseTag> tags) {        
        this(pubKey, tags, "Deletion request");
    }
}
