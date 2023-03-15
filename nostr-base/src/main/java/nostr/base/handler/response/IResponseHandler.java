package nostr.base.handler.response;

import nostr.base.Command;
import nostr.base.handler.IHandler;

/**
 *
 * @author eric
 */
public interface IResponseHandler extends IHandler {

    public Command getCommand();

}
