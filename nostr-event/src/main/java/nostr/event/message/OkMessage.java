
package nostr.event.message;

import nostr.base.Command;
import nostr.event.BaseMessage;

/**
 *
 * @author squirrel
 */
public class OkMessage extends BaseMessage {

    private final String eventId;
    private final Boolean flag;
    private final String message;

    public OkMessage(String eventId, Boolean flag, String message) {
        super(Command.OK);
        this.eventId = eventId;
        this.flag = flag;
        this.message = message;
    }
    
    
}
