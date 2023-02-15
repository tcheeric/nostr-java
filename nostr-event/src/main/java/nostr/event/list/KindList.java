
package nostr.event.list;

import nostr.event.Kind;
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
public class KindList extends BaseList<Kind> {

    public KindList() {
        this(new ArrayList<>());
    }

    private KindList(@NonNull List<Kind> list) {
        super(list);
    }
}
