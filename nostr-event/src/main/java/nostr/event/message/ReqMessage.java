
package nostr.event.message;

import nostr.event.BaseMessage;
import nostr.base.Command;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.event.list.FiltersList;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends BaseMessage {

    private final String subscriptionId;
    private final FiltersList filtersList;

    public ReqMessage(String subscriptionId, FiltersList filtersList) {
        super(Command.REQ);
        this.subscriptionId = subscriptionId;
        this.filtersList = filtersList;
    }
}
