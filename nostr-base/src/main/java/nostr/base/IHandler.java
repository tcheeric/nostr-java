
package nostr.base;

import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IHandler {

    public abstract void process() throws NostrException;
    
}
