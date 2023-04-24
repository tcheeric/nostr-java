package nostr.ws.base.handler.command;

import nostr.ws.base.handler.ICommandHandler;

/**
 *
 * @author eric
 */
public interface IEoseCommandHandler extends ICommandHandler {

    public void setSubscriptionId(String subscriptionId);
}
