
package nostr.ws.handler;

import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.base.IHandler;

/**
 *
 * @author squirrel
 */
@Log
public abstract class BaseHandler implements IHandler {

    @Override
    public void process() {
        log.log(Level.INFO, "process");
    }
    
}
