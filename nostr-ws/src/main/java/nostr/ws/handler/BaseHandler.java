
package nostr.ws.handler;

import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.ws.base.handler.request.IRequestHandler;

/**
 *
 * @author squirrel
 */
@Log
public abstract class BaseHandler implements IRequestHandler {

    @Override
    public void process() {
        log.log(Level.INFO, "process");
    }
    
}
