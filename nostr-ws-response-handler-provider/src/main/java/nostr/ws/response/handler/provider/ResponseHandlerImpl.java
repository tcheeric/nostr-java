/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nostr.ws.response.handler.provider;

import java.util.ServiceLoader;
import java.util.logging.Level;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.base.annotation.DefaultHandler;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.types.values.impl.ArrayValue;
import nostr.util.NostrException;
import nostr.ws.handler.command.spi.ICommandHandler;
import nostr.ws.handler.command.spi.ICommandHandler.Reason;
import nostr.ws.handler.spi.IResponseHandler;

/**
 *
 * @author eric
 */
@Data
@DefaultHandler
@Log
public class ResponseHandlerImpl implements IResponseHandler {

    private final ICommandHandler commandHandler;

    public ResponseHandlerImpl() {

        this.commandHandler = ServiceLoader
                .load(ICommandHandler.class).stream().map(p -> p.get())
                .filter(ch -> !ch.getClass().isAnnotationPresent(DefaultHandler.class))
                .findFirst()
                .get();
                
        
        log.log(Level.INFO, ">>> {0}", commandHandler);
//                .stream()
//                .map(ServiceLoader.Provider::get)
//                .filter(ch -> !ch.getClass().isAnnotationPresent(DefaultHandler.class))
//                .findFirst()
//                .get();
    }

    @Override
    public void process(String message, Relay relay) throws NostrException {
        ArrayValue jsonArr = new JsonArrayUnmarshaller(message).unmarshall();
        final String command = (jsonArr).get(0).get().getValue().toString();

        switch (command) {
            case "EOSE" -> {
                var subId = (jsonArr).get(1).get().getValue().toString();
                commandHandler.onEose(subId, relay);
            }
            case "OK" -> {
                String eventId = (jsonArr).get(1).get().getValue().toString();
                boolean result = Boolean.parseBoolean((jsonArr).get(2).toString());
                String msg = (jsonArr).get(3).get().getValue().toString();
                final var msgSplit = msg.split(":", 2);
                Reason reason;
                String reasonMessage = msg;
                if (msgSplit.length < 2) {
                    reason = Reason.UNDEFINED;
                } else {
                    reason = Reason.fromCode(msgSplit[0]).orElseThrow(RuntimeException::new);
                    reasonMessage = msgSplit[1];
                }

                commandHandler.onOk(eventId, reasonMessage, reason, result, relay);
            }
            case "NOTICE" -> {
                var param = jsonArr.get(1).get().getValue().toString();
                commandHandler.onNotice(param);
            }
            case "EVENT" -> {
                var subId = jsonArr.get(1).get().getValue().toString();
                var jsonEvent = jsonArr.get(2).get().toString();

                commandHandler.onEvent(jsonEvent, subId, relay);
            }
            case "AUTH" -> {
                var challenge = jsonArr.get(1).get().getValue().toString();

                commandHandler.onAuth(challenge, relay);
            }
            default -> {
                throw new AssertionError();
            }
        }
    }
}
