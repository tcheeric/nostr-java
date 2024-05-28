
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

    public FiltersList(Class<T> clazz) {
        this(new ArrayList<>(), clazz);
    }

    public FiltersList(Class<T> clazz, T... filters) {
        this(List.of(filters), clazz);
    }

    public FiltersList(@NonNull List<T> list, Class<T> clazz) {
        super.addAll(list);
        this.clazz = clazz;
    }
}
