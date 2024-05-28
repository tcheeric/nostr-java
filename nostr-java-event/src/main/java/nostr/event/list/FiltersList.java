
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import nostr.base.INostrList;
import nostr.event.impl.Filters;
import nostr.event.json.deserializer.CustomFiltersListDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Builder
@JsonDeserialize(using = CustomFiltersListDeserializer.class)
public class FiltersList<T extends Filters> extends INostrList<T> {
    private final Class<T> clazz;

    public FiltersList() {
        this(new ArrayList<>());
    }

    public FiltersList(Class<T> clazz) {
        this(new ArrayList<>(), clazz);
    }

    public FiltersList(T... filters) {
        this(List.of(filters));
    }

    public FiltersList(@NonNull List<T> list) {
        this(list, (Class<T>) Filters.class);
    }

    public FiltersList(@NonNull List<T> list, Class<T> clazz) {
        super.addAll(list);
        this.clazz = clazz;
    }
}
