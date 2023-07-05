
package nostr.event.list;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import nostr.base.annotation.JsonList;
import nostr.event.Kind;

/**
 *
 * @author squirrel
 */
@Builder
@JsonList
public class KindList extends BaseList<Kind> {

    public KindList() {
        this(new ArrayList<>());
    }

    private KindList(@NonNull List<Kind> list) {
        super(list);
    }
}
