package nostr.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import nostr.base.IElement;

/**
 *
 * @author squirrel
 */
@Getter
public abstract class BaseMessage implements IElement {
    private final ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
    private final String command;

    protected BaseMessage(String command) {
        this.command = command;
    }

    @Override
    public Integer getNip() {
        return 1;
    }

    public abstract String encode() throws JsonProcessingException;
}
