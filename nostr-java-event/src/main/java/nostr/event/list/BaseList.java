package nostr.event.list;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.base.INostrList;
import nostr.base.annotation.JsonList;
import nostr.event.json.serializer.CustomBaseListSerializer;

/**
 *
 * @author squirrel
 * @param <T>
 */
@AllArgsConstructor
@Data
@JsonList
@JsonSerialize(using = CustomBaseListSerializer.class)
public abstract class BaseList<T> implements INostrList<T> {

    @NonNull
    private final List<T> list;

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
        aList.stream().forEach(e -> this.list.add(e));
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
