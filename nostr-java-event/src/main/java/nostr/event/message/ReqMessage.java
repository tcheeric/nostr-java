
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;
import nostr.event.list.FiltersList;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends BaseMessage {

    @JsonProperty
    private final String subscriptionId;
    
    @JsonProperty
    private final FiltersList filtersList;

    public ReqMessage(String subscriptionId, Filters filters) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = new FiltersList();
        this.filtersList.add(filters);
    }

    public ReqMessage(String subscriptionId, FiltersList filtersList) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = filtersList;
    }
}
