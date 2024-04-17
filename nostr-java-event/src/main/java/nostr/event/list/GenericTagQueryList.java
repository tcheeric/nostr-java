package nostr.event.list;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.NonNull;
import nostr.base.GenericTagQuery;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Builder
public class GenericTagQueryList extends BaseList<GenericTagQuery> {

    public GenericTagQueryList() {
        this(new ArrayList<>());
    }

    public GenericTagQueryList(GenericTagQuery... queries) {
        super(queries);
    }

    private GenericTagQueryList(@NonNull List<GenericTagQuery> list) {
        super(list);
    }

    @Override
    @JsonIgnore
    public Integer getNip() {
        return 1;
    }

}
