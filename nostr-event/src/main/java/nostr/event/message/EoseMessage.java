
package nostr.event.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class EoseMessage extends GenericMessage {

    private final String subscriptionId;

    public EoseMessage(String subId) {
        super(Command.EOSE.name());
        this.subscriptionId = subId;
    }
    
}
