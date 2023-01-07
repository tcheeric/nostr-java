
package nostr.base;

import nostr.util.NostrException;

/**
 *
 * @author squirrel
 * @param <T>
 */
public interface IUnmarshaller<T> {

    public abstract T unmarshall() throws NostrException;

}
