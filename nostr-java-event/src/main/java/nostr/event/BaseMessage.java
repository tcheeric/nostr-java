package nostr.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.IElement;
import nostr.event.message.GenericMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
