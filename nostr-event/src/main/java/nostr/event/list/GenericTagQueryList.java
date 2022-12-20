package nostr.event.list;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.event.impl.GenericTagQuery;

/**
 *
 * @author squirrel
 */
@Builder
@Log
public class GenericTagQueryList extends BaseList<GenericTagQuery> {

    public GenericTagQueryList() {
        this(new ArrayList<>());
    }

    private GenericTagQueryList(@NonNull List<GenericTagQuery> list) {
        super(list);
    }

}
