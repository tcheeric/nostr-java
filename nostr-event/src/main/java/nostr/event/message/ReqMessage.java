
package nostr.event.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.GenericMessage;
import nostr.event.list.FiltersList;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends GenericMessage {

    private final String subscriptionId;
    private final FiltersList filtersList;

    public ReqMessage(String subscriptionId, FiltersList filtersList) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = filtersList;
    }
}
