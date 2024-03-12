package nostr.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import nostr.base.IElement;

/**
 *
 * @author squirrel
 */
@Data
@AllArgsConstructor
@ToString
public abstract class BaseMessageNick implements IElement {

    private final String command;

    protected BaseMessageNick() {
        this.command = null;
    }

    @Override
    public Integer getNip() {
        return 1;
    }

}
