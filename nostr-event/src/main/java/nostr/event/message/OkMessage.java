
package nostr.event.message;

import nostr.base.Command;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author squirrel
 */
public class OkMessage extends GenericMessage {

    private final String eventId;
    private final Boolean flag;
    private final String message;

    public OkMessage(String eventId, Boolean flag, String message) {
        super(Command.OK.name());
        this.eventId = eventId;
        this.flag = flag;
        this.message = message;
    }
    
    
}
