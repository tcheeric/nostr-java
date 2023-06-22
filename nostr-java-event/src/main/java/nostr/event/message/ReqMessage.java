
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
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
