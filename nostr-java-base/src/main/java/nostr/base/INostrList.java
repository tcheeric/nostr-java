
package nostr.base;

import java.util.List;

/**
 *
 * @author squirrel
 * @param <T>
 */
public interface INostrList<T> extends IElement {

    void add(T elt);

    void addAll(INostrList<T> list);

    List<T> getList();
    
    int size();
}
