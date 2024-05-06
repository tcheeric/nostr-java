package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.base.INostrList;
import nostr.event.json.serializer.CustomBaseListSerializer;

import java.util.List;

/**
 *
 * @author squirrel
 * @param <T>
 */
// TODO: Why are we using this instead of just use a regular java collection?
@AllArgsConstructor
@Data
@JsonSerialize(using = CustomBaseListSerializer.class)
public abstract class BaseList<T> implements INostrList<T> {

    @NonNull
    private final List<T> list;

    public BaseList(T... elements) {
        this.list = List.of(elements);
    }

    @Override
    public void add(@NonNull T elt) {
        if (!list.contains(elt)) {
            this.list.add(elt);
        }
    }

    @Override
    public void addAll(@NonNull INostrList<T> aList) {
        this.list.addAll(aList.getList());
    }

    public void addAll(@NonNull List<T> aList) {
        this.list.addAll(aList);
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
