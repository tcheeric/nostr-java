
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.Command;
import nostr.base.IEvent;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class EventMessage extends GenericMessage {

    @JsonProperty
    private final IEvent event;
    
    @JsonProperty
    private String subscriptionId;

    public EventMessage(@NonNull IEvent event) {
        this(event, null);
    }

    public EventMessage(@NonNull IEvent event, String subscriptionId) {
        super(Command.EVENT.name());
        this.event = event;
        this.subscriptionId = subscriptionId;
    }
}
