package nostr.event.message;

import lombok.NonNull;
import nostr.event.BaseMessage;

/**
 *
 * @author eric
 */
public abstract class BaseAuthMessage extends BaseMessage {

    public BaseAuthMessage(String command) {
        super(command);
    }

    @Override
    public Integer getNip() {
        return 42;
    }
}
