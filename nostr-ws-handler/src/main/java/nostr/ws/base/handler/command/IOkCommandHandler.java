package nostr.ws.base.handler.command;

import java.util.Arrays;
import java.util.Optional;
import nostr.ws.base.handler.ICommandHandler;

/**
 *
 * @author eric
 */
public interface IOkCommandHandler extends ICommandHandler {
    
    public enum Reason {
        UNDEFINED(""),
        DUPLICATE("duplicate"),
        BLOCKED("blocked"),
        INVALID("invalid"),
        RATE_LIMITED("rate-limited"),
        ERROR("error"),
        POW("pow");

        public static Reason valueOf(Object param) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

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
