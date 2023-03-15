
package nostr.ws.handler;

import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@Log
public class DefaultErrorHandler extends BaseHandler {

    private final Throwable cause;

    @Override
    public void process() {
        log.log(Level.SEVERE, cause.getMessage());        
    }

}
