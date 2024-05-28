package nostr.event.list;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;
import nostr.base.GenericTagQuery;
import nostr.base.INostrList;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
public class GenericTagQueryList<T extends GenericTagQuery> extends INostrList<T> {
    private final Class<T> clazz;

    public GenericTagQueryList() {
        this(new ArrayList<>());
    }

    public GenericTagQueryList(T... queries) {
        this(List.of(queries));
    }

    public GenericTagQueryList(@NonNull List<T> list) {
        this(list, (Class<T>) GenericTagQuery.class);
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
