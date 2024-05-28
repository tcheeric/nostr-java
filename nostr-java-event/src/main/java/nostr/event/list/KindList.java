
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import nostr.base.INostrList;
import nostr.event.json.deserializer.CustomKindListDeserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author squirrel
 */
@Builder
@JsonDeserialize(using = CustomKindListDeserializer.class)
public class KindList<Integer> extends INostrList<Integer> {

    public KindList() {
        this(new ArrayList<>());
    }

    public KindList(Integer... kinds) {
        this(List.of(kinds));
    }

    public KindList(@NonNull List<Integer> list) {
        super.addAll(list);
    }
    @Override
    public boolean add(Integer[] elt) {
        return super.addAll(Stream.of(elt).filter(Objects::nonNull).toList());
    }
}
