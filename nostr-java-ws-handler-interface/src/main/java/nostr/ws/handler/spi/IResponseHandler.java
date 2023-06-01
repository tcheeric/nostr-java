package nostr.ws.handler.spi;

import nostr.base.IHandler;
import lombok.NonNull;
import nostr.base.Relay;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IResponseHandler extends IHandler {
    
    public abstract void process(@NonNull String message, Relay relay) throws NostrException;

}
