/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nostr.ws.response.handler.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    private final ICommandHandler commandHandler;

    public ResponseHandlerImpl() {

        this.commandHandler = ServiceLoader
                .load(ICommandHandler.class).stream().map(p -> p.get())
                .filter(ch -> !ch.getClass().isAnnotationPresent(DefaultHandler.class))
                .findFirst()
                .get();
    }

    @Override
    public void process(String message, Relay relay) throws NostrException {

        log.log(Level.INFO, "Process Message: {0} from relay: {1}", new Object[]{message, relay});

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> items;
        try {
            var listObj = objectMapper.readValue(message, new TypeReference<List<Object>>() {});
            items = listObj.stream().map(Object::toString).collect(Collectors.toList());
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }

        final String command = items.get(0);

        switch (command) {
            case "EOSE" -> {
                var subId = items.get(1);
                commandHandler.onEose(subId, relay);
            }
            case "OK" -> {
                String eventId = items.get(1);
                boolean result = Boolean.parseBoolean(items.get(2));
                String msg = items.get(3);
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
                var param = items.get(1);
                commandHandler.onNotice(param);
            }
            case "EVENT" -> {
                var subId = items.get(1);
                var jsonEvent = items.get(2);

                commandHandler.onEvent(jsonEvent, subId, relay);
            }
            case "AUTH" -> {
                var challenge = items.get(1);

                commandHandler.onAuth(challenge, relay);
            }
            default -> {
                throw new AssertionError();
            }
        }
    }
}
