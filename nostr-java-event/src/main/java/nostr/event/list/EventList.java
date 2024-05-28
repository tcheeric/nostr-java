
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
@JsonDeserialize(using = CustomEventListDeserializer.class)
public class EventList<T extends GenericEvent> extends BaseList<T> {
    private final Class<T> clazz;

    public EventList() {
        this(new ArrayList<>());
    }

    public EventList(Class<T> clazz) {
        this(new ArrayList<>(), clazz);
    }

    public EventList(T... events) {
        this(List.of(events));
    }

    public EventList(@NonNull List<T> list) {
        this(list, (Class<T>) GenericEvent.class);
    }

    public EventList(@NonNull List<T> list, Class<T> clazz) {
        super(list);
        this.clazz = clazz;
    }
}
