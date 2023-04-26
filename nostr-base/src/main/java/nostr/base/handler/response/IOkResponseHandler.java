package nostr.base.handler.response;

import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author squirrel
 */
public interface IOkResponseHandler extends IResponseHandler {
    
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
    
    public void setEventId(String eventId);
    
    public void setResult(boolean result);
    
    public void setReason(Reason reason);
    
    public void setMessage(String message);
}
