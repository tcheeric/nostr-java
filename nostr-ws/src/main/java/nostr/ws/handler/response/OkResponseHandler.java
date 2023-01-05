package nostr.ws.handler.response;

import nostr.base.Command;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.Optional;
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
        UNDEFINED(""),
        DUPLICATE("duplicate"),
        BLOCKED("blocked"),
        INVALID("invalid"),
        RATE_LIMITED("rate-limited"),
        ERROR("error"),
        POW("pow");

        private final String code;

        Reason(String code) {
            this.code = code;
        }

        public static Optional<Reason> fromCode(String code) {
            return Arrays.stream(values())
                    .filter(reason -> reason.code.equalsIgnoreCase(code))
                    .findFirst();
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
