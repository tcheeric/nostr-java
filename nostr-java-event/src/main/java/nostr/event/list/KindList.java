
package nostr.event.list;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;

/**
 *
 * @author squirrel
 */
@Builder
public class KindList extends BaseList<Integer> {

    public KindList() {
        this(new ArrayList<>());
    }

    public KindList(@NonNull List<Integer> list) {
        super(new ArrayList<>(list));
    }
}
