package nostr.ws.base.handler.command;

import nostr.base.Relay;
import nostr.ws.base.handler.ICommandHandler;

/**
 *
 * @author eric
 */
public interface IAuthCommandHandler extends ICommandHandler {
    
    public void setChallenge(String challenge);

    public void setRelay(Relay relay);
}
