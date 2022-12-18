
package com.tcheeric.nostr.controller.handler;

import com.tcheeric.nostr.controller.IHandler;
import java.util.logging.Level;
import lombok.extern.java.Log;

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
