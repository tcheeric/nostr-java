
package nostr.event.message;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.BaseMessage;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class EoseMessage extends BaseMessage {

    private final String subscriptionId;

    public EoseMessage(String subId) {
        super(Command.EOSE);
        this.subscriptionId = subId;
    }
    
}
