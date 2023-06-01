
package nostr.event.list;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import nostr.base.ITag;
import nostr.base.annotation.JsonList;

/**
 *
 * @author squirrel
 */
@SuppressWarnings("rawtypes")
@JsonList
public class TagList extends BaseList {

    @SuppressWarnings("unchecked")
    public TagList() {
        this(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    protected TagList(@NonNull List<? extends ITag> list) {
        super(list);
    }

}
