package nostr.base.handler.response;

import nostr.base.Relay;

/**
 *
 * @author eric
 */
public interface IAuthResponseHandler extends IResponseHandler {
    
    public void setChallenge(String challenge);

    public void setRelay(Relay relay);
}
