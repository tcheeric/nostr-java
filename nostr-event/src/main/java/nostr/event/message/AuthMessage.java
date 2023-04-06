
package nostr.event.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.base.IEvent;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class AuthMessage extends GenericMessage {

    private final IEvent event;
    
    public AuthMessage(IEvent event) {
        super(Command.AUTH.name());
        this.event = event;
    }
    
}
