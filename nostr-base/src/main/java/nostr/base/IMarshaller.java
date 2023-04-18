
package nostr.base;

import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IMarshaller {
    
    public abstract String marshall() throws NostrException;    
}
