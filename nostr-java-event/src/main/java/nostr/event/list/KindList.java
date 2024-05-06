
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import nostr.event.json.deserializer.CustomKindListDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Builder
@JsonDeserialize(using = CustomKindListDeserializer.class)
public class KindList extends BaseList<Integer> {

    public KindList() {
        this(new ArrayList<>());
    }

    public KindList(Integer... kinds) {
        super(kinds);
    }

    public KindList(@NonNull List<Integer> list) {
        super(new ArrayList<>(list));
    }
}
