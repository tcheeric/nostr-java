package nostr.ws.handler.response;

import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.Relay;
import nostr.util.NostrException;
import nostr.ws.base.handler.response.IAuthResponseHandler;

/**
 *
 * @author eric
 */
@Builder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Log
public class DefaultAuthResponseHandler implements IAuthResponseHandler {

    private String challenge;
    private Relay relay;

    public DefaultAuthResponseHandler(@NonNull String challenge, @NonNull Relay relay) {
        this.challenge = challenge;
        this.relay = relay;
    }

    @Override
    public Command getCommand() {
        return Command.AUTH;
    }

    @Override
    public void process() throws NostrException {
        log.log(Level.INFO, "{0}", this);
    }
}
