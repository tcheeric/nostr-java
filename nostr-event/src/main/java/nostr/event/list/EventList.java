
package nostr.event.list;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import nostr.base.annotation.JsonList;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author squirrel
 */
@Builder
@JsonList
public class EventList extends BaseList<GenericEvent> {

    public EventList() {
        this(new ArrayList<>());
    }

    private EventList(@NonNull List<GenericEvent> list) {
        super(list);
    }
}
