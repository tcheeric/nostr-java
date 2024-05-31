package nostr.event;

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

    @Override
    public Integer getNip() {
        return 1;
    }

}
