package nostr.event.message;

import nostr.event.BaseMessage;

/**
 *
 * @author eric
 */
public abstract class BaseAuthMessage extends BaseMessage {

    protected BaseAuthMessage(){
        super();
    }

    public BaseAuthMessage(String command) {
        super(command);
    }

    @Override
    public Integer getNip() {
        return 42;
    }
}
