package nostr.ws.handler.spi;

import nostr.base.IHandler;
import nostr.base.Relay;
import nostr.event.impl.GenericMessage;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IRequestHandler extends IHandler {

    public abstract void process(GenericMessage message, Relay relay) throws NostrException;
}
