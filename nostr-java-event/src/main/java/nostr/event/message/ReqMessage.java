
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
public class ReqMessage<T extends Filters> extends BaseMessage {

    @JsonProperty
    private final String subscriptionId;
    
    @JsonProperty
    private final FiltersList<T> filtersList;

    public ReqMessage(String subscriptionId, T filters, Class<T> clazz) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = new FiltersList<>(clazz);
        this.filtersList.add(filters);
    }

    public ReqMessage(String subscriptionId, FiltersList<T> filtersList) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = filtersList;
    }
}
