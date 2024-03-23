/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package nostr.ws.handler.command.spi;

import nostr.base.IHandler;
import nostr.base.Relay;

import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author eric
 */
public interface ICommandHandler extends IHandler {

    void onEose(String subId, Relay relay);

    void onOk(String eventId, String reasonMessage, Reason reason, boolean result, Relay relay);

    void onNotice(String message);

    void onEvent(String jsonEvent, String subId, Relay relay);

    void onAuth(String challenge, Relay relay);

    enum Reason {
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
