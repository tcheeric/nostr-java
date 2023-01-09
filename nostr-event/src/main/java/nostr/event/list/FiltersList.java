
package nostr.event.list;

import nostr.event.impl.Filters;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.JsonList;

/**
 *
 * @author squirrel
 */
@Builder
@Log
@JsonList
public class FiltersList extends BaseList<Filters> {

    public FiltersList() {
        this(new ArrayList<>());
    }

    private FiltersList(@NonNull List<Filters> list) {
        super(list);
    }
}
