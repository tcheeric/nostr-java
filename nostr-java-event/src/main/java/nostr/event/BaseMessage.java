package nostr.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import nostr.base.IElement;

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

    public abstract String encode() throws JsonProcessingException;
}
