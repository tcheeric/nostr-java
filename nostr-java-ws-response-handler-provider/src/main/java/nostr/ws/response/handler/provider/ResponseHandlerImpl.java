/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nostr.ws.response.handler.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.logging.Level;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.base.annotation.DefaultHandler;
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

    private ICommandHandler commandHandler;

    public ResponseHandlerImpl() {

        try {
            this.commandHandler = ServiceLoader
                    .load(ICommandHandler.class).stream().map(p -> p.get())
                    .filter(ch -> !ch.getClass().isAnnotationPresent(DefaultHandler.class))
                    .findFirst()
                    .get();
        } catch (NoSuchElementException ex) {
            log.log(Level.WARNING, "No custom command handler provided. Using default command handler");
            this.commandHandler = ServiceLoader
                    .load(ICommandHandler.class).stream().map(p -> p.get())
                    .filter(ch -> ch.getClass().isAnnotationPresent(DefaultHandler.class))
                    .findFirst()
                    .get();
        }
    }

    @Override
    public void process(String message, Relay relay) throws NostrException {

        log.log(Level.INFO, "Process Message: {0} from relay: {1}", new Object[]{message, relay});

        Object[] items = unmarshall(message);
        final String command = items[0].toString();

        switch (command) {
            case "EOSE" -> {
                var subId = items[1].toString();
                commandHandler.onEose(subId, relay);
            }
            case "OK" -> {
                String eventId = items[1].toString();
                boolean result = Boolean.parseBoolean(items[2].toString());
                String msg = items[3].toString();
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
                var param = items[1].toString();
                commandHandler.onNotice(param);
            }
            case "EVENT" -> {
                try {
                    var subId = items[1].toString();
                    var jsonEvent = new ObjectMapper().writeValueAsString(items[2]);
                    commandHandler.onEvent(jsonEvent, subId, relay);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
            }

            case "AUTH" -> {
                var challenge = items[1].toString();

                commandHandler.onAuth(challenge, relay);
            }
            default -> {
                throw new AssertionError();
            }
        }
    }

    private Object[] unmarshall(String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, Object[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
