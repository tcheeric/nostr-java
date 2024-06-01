
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ReqMessage extends BaseMessage {

    @JsonProperty
    private final String subscriptionId;

    @JsonProperty
    private final List<Filters> filtersList;

    public ReqMessage(String subscriptionId, Filters filters) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = new ArrayList<>();
        this.filtersList.add(filters);
    }

    public ReqMessage(String subscriptionId, List<Filters> incomingFiltersList) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filtersList = new ArrayList<>();
        this.filtersList.addAll(incomingFiltersList);
    }
}
