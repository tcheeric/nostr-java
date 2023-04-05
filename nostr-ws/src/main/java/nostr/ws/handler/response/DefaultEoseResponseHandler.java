
package nostr.ws.handler.response;

import nostr.base.Command;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.handler.response.IEoseResponseHandler;

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
public class DefaultEoseResponseHandler implements IEoseResponseHandler {

    private String subscriptionId;

    public DefaultEoseResponseHandler(@NonNull String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public void process() {
        log.log(Level.INFO, "{0}", this);
    }

    @Override
    public Command getCommand() {
        return Command.EOSE;
    }
    
}
