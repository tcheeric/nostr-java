package nostr.event.list;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.NonNull;
import nostr.base.GenericTagQuery;
import nostr.base.INostrList;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Builder
public class GenericTagQueryList<T extends GenericTagQuery> extends INostrList<T> {
    private final Class<T> clazz;

    public GenericTagQueryList(Class<T> clazz) {
        this(new ArrayList<>(), clazz);
    }

    public GenericTagQueryList(Class<T> clazz, T... queries) {
        this(List.of(queries), clazz);
    }

    private GenericTagQueryList(@NonNull List<T> list, Class<T> clazz) {
        super.addAll(list);
        this.clazz = clazz;
    }

    @JsonIgnore
    public Integer getNip() {
        return 1;
    }

}
