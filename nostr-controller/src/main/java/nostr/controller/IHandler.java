
package nostr.controller;

import nostr.base.NostrException;
import java.io.IOException;

/**
 *
 * @author squirrel
 */
public interface IHandler {

    public abstract void process() throws IOException, NostrException;
    
}
