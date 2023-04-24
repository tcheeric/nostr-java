package nostr.ws.base.handler;

import lombok.NonNull;
import nostr.base.Relay;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public interface IResponseHandler extends IHandler {

    public abstract void process(@NonNull String message, Relay relay) throws NostrException;

}
