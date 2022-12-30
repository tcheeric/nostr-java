package nostr.ws.handler.response;

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

    public enum Reason {
        BLOCKED("blocked"),
        INVALID("invalid"),
        RATE_LIMITED("rate-limited"),
        ERROR("error"),
        POW("pow");

        private final String code;

        private Reason(String code) {
            this.code = code;
        }
    }
    
    private final String eventId;
    private final boolean result;
    private final Reason reason;
    private final String message;

    public OkResponseHandler(String eventId, boolean result, Reason reason, String message) {
        super(Command.OK);
        this.eventId = eventId;
        this.result = result;
        this.reason = reason;
        this.message = message;
    }

    @Override
    public void process() {
        log.log(Level.INFO, "{0}", this);
    }
}
