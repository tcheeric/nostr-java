
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
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
public class FiltersList extends BaseList<Filters> {

    public FiltersList() {
        this(new ArrayList<>());
    }

    public FiltersList(Filters... filters) {
        super(filters);
    }

    public FiltersList(@NonNull List<Filters> list) {
        super(list);
    }
}
