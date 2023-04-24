package nostr.ws.base.handler.command;

import nostr.ws.base.handler.ICommandHandler;

/**
 *
 * @author eric
 */
public interface IEventCommandHandler extends ICommandHandler {
    
    public void setSubscriptionId(String subId);
    
    public void setJsonEvent(String jsonEvent);
}
