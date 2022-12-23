
package nostr.controller;

import java.io.IOException;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IHandler {

    public abstract void process() throws IOException, NostrException;
    
}
