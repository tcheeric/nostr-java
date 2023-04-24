package nostr.ws.base.handler.command;

import nostr.ws.base.handler.ICommandHandler;

/**
 *
 * @author eric
 */
public interface INoticeCommandHandler extends ICommandHandler {

    public void setMessage(String message);
    
}
