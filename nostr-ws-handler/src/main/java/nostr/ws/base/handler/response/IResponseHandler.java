package nostr.ws.base.handler.response;

import nostr.base.Command;
import nostr.ws.base.handler.IHandler;

/**
 *
 * @author eric
 */
public interface IResponseHandler extends IHandler {

    public Command getCommand();

}
