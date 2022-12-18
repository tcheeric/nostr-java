
package nostr.event.marshaller;

import nostr.base.NostrException;

/**
 *
 * @author squirrel
 */
public interface IMarshaller {
    
    public abstract String marshall() throws NostrException;
}
