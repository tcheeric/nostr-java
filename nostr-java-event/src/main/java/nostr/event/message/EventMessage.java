
package nostr.event.message;

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

    private final IEvent event;

    public EventMessage(@NonNull IEvent event) {
        super(Command.EVENT.name());
        this.event = event;
    }
}
