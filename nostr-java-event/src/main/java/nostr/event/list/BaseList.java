package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NonNull;
import nostr.base.IElement;
import nostr.base.INostrList;
import nostr.event.BaseEvent;
import nostr.event.json.serializer.CustomBaseListSerializer;

import java.util.List;

/**
 *
 * @author squirrel
 * @param <T>
 */
// TODO: Why are we using this instead of just use a regular java collection?
@Data
@JsonSerialize(using = CustomBaseListSerializer.class)
public abstract class BaseList<T extends BaseEvent> extends INostrList<T> implements IElement {

    @NonNull
    private final List<T> list;

    public BaseList(T... elements) {
        this(List.of(elements));
    }

    public BaseList(List<T> elements) {
        this.list = elements;
    }

    @Override
    public boolean add(@NonNull T elt) {
        if (!list.contains(elt)) {
            this.list.add(elt);
        }
        return false;
    }

    @Override
    public boolean addAll(@NonNull List<T> aList) {
        return this.list.addAll(aList);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public Integer getNip() {
        return 1;
    }

}
