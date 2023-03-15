package nostr.ws.handler.response;

import nostr.base.Command;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.handler.response.IOkResponseHandler;

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
public class DefaultOkResponseHandler implements IOkResponseHandler {
    
    private String eventId;
    private boolean result;
    private IOkResponseHandler.Reason reason;
    private String message;

    public DefaultOkResponseHandler(String eventId, boolean result, IOkResponseHandler.Reason reason, String message) {
        this.eventId = eventId;
        this.result = result;
        this.reason = reason;
        this.message = message;
    }

    @Override
    public void process() {
        log.log(Level.INFO, "{0}", this);
    }

    @Override
    public Command getCommand() {
        return Command.OK;
    }
}
