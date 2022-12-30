
package nostr.ws.handler.response;

import nostr.base.Command;
import java.io.IOException;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Log
public class EventResponseHandler extends BaseResponseHandler {
    
    private final String subscriptionId;
    private final String jsonEvent;

    public EventResponseHandler(String subscriptionId, String jsonEvent) {
        super(Command.EVENT);
        this.subscriptionId = subscriptionId;
        this.jsonEvent = jsonEvent;
    }

    @Override
    public void process() throws IOException, NostrException {
        log.log(Level.INFO, "{0}", this);
    }
}
