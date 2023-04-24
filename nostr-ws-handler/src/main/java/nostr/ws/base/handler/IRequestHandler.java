package nostr.ws.base.handler;

import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public interface IRequestHandler extends IHandler {
    
    public abstract void process() throws NostrException;
}
