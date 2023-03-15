
package nostr.ws.handler.response;

import nostr.base.Command;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.handler.response.IEventResponseHandler;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Builder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Log
public class DefaultEventResponseHandler implements IEventResponseHandler{
    
    private String subscriptionId;
    private String jsonEvent;

    public DefaultEventResponseHandler(String subscriptionId, String jsonEvent) {
        this.subscriptionId = subscriptionId;
        this.jsonEvent = jsonEvent;
    }

    @Override
    public void process() throws NostrException {
        log.log(Level.INFO, "{0}", this);
    }

    @Override
    public Command getCommand() {
        return Command.EVENT;
    }
}
