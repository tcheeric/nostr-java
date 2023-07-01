/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package nostr.ws.handler.command.spi;

import java.util.Arrays;
import java.util.Optional;
import nostr.base.IHandler;
import nostr.base.Relay;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public interface ICommandHandler extends IHandler {

    public abstract void onEose(String subId, Relay relay);

    public abstract void onOk(String eventId, String reasonMessage, Reason reason, boolean result, Relay relay);

    public abstract void onNotice(String message);

    public abstract void onEvent(String jsonEvent, String subId, Relay relay);

    public abstract void onAuth(String challenge, Relay relay) throws NostrException;

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
}
