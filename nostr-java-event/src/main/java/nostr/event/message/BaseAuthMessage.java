package nostr.event.message;

import nostr.event.impl.GenericMessage;

/**
 *
 * @author eric
 */
public abstract class BaseAuthMessage extends GenericMessage {
    
    public BaseAuthMessage(String command) {
        super(command);
    }
    
}
