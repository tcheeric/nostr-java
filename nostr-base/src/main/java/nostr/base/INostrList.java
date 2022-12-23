
package nostr.base;

import java.util.List;

/**
 *
 * @author squirrel
 * @param <T>
 */
public interface INostrList<T> extends IElement {

    public abstract void add(T elt);

    public abstract void addAll(INostrList<T> list);

    public abstract List<T> getList();
    
    public abstract int size();
}
