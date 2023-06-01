
package nostr.event.impl;

import java.util.List;
import nostr.event.Kind;
import nostr.base.PublicKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;
import nostr.event.tag.EventTag;

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

    public DeletionEvent(PublicKey pubKey, List<GenericEvent> eventsToDelete) {
        super(pubKey, Kind.DELETION);
        eventsToDelete.stream().map(e -> e.getId()).forEach(eId -> addTag(EventTag.builder().idEvent(eId).build()));
    }
}
