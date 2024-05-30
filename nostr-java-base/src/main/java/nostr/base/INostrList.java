package nostr.base;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 * @param <T>
 */
public abstract class INostrList<T extends IElement> extends ArrayList<T> {

    public boolean add(T... elt) {
        return this.addAll(List.of(elt));
    }

    public boolean addAll(List<T> list) {
        return super.addAll(list);
    }

    public List<T> getList() {
        return super.stream().toList();
    }
    
    public int size() {
        return super.size();
    }
}
