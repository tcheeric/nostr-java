
package nostr.controller.handler.response;

import nostr.base.Command;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Log
public class OkResponseHandler extends BaseResponseHandler {

    private final String eventId;
    private final boolean blocked;
    private final String message;

    public OkResponseHandler(String eventId, boolean blocked, String message) {
        super(Command.OK);
        this.eventId = eventId;
        this.blocked = blocked;
        this.message = message;
    }

    @Override
    public void process() {
        log.log(Level.FINE, "handle");
    }
}
