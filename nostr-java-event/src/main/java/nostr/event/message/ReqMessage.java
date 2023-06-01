
package nostr.event.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends GenericMessage {

    private final String subscriptionId;
    private final Filters filters;

    public ReqMessage(String subscriptionId, Filters filters) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filters = filters;
    }
}
