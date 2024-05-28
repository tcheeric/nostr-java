
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

    public ReqMessage(String subscriptionId, T filters) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = new FiltersList<>((Class<T>) Filters.class);
        this.filtersList.add(filters);
    }

    public ReqMessage(String subscriptionId, FiltersList<T> incomingFiltersList) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = new FiltersList<>((Class<T>) Filters.class);
        this.filtersList.addAll(incomingFiltersList);
    }
}
