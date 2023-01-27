
package nostr.event.list;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.JsonList;
import nostr.base.list.BaseList;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author squirrel
 */
@Builder
@Log
@JsonList
public class EventList extends BaseList<GenericEvent> {

    public EventList() {
        this(new ArrayList<>());
    }

    private EventList(@NonNull List<GenericEvent> list) {
        super(list);
    }    
}
