
package nostr.ws.handler.response;

import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.handler.response.INoticeResponseHandler;

/**
 *
 * @author squirrel
 */
@Builder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Log
public class DefaultNoticeResponseHandler implements INoticeResponseHandler {
    
    private String message;

    public DefaultNoticeResponseHandler(String message) {
        this.message = message;
    }    

    @Override
    public void process() {
        log.log(Level.WARNING, "{0}", this);
    }    

    @Override
    public Command getCommand() {
        return Command.NOTICE;
    }
}
