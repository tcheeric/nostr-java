
package nostr.ws.handler.response;

import nostr.base.Command;
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
@EqualsAndHashCode(callSuper = false)
@ToString
@Log
public class NoticeResponseHandler extends BaseResponseHandler {
    
    private final String message;

    public NoticeResponseHandler(String message) {
        super(Command.NOTICE);
        this.message = message;
    }    

    @Override
    public void process() {
        log.log(Level.WARNING, "{0}", this);
    }    
}
