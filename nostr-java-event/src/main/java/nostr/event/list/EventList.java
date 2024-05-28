
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.event.json.deserializer.CustomEventListDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Builder
// TODO - public class EventList extends BaseList<? extends GenericEvent>
@JsonDeserialize(using = CustomEventListDeserializer.class)
public class EventList extends BaseList<GenericEvent> {

    public EventList() {
        this(new ArrayList<>());
    }

    public EventList(GenericEvent... events) {
        super(events);
    }

    public EventList(@NonNull List<GenericEvent> list) {
        super(list);
    }
}
