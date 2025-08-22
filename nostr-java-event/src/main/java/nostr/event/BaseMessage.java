package nostr.event;

import lombok.Getter;
import nostr.base.IElement;
import nostr.event.json.codec.EventEncodingException;

/**
 *
 * @author squirrel
 */
@Getter
public abstract class BaseMessage implements IElement {
    private final String command;

    protected BaseMessage(String command) {
        this.command = command;
    }

    public abstract String encode() throws EventEncodingException;
}
