
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;

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
    private final Filters filters;

    public ReqMessage(String subscriptionId, Filters filters) {
        super(Command.REQ.name());
        this.subscriptionId = subscriptionId;
        this.filters = filters;
    }
}
