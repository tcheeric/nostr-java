/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nostr.ws.response.handler.impl;

import lombok.Builder;
import lombok.Data;
import nostr.base.Relay;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.types.values.impl.ArrayValue;
import nostr.util.NostrException;
import nostr.ws.base.handler.IResponseHandler;
import nostr.ws.base.handler.ICommandHandler;
import nostr.ws.base.handler.command.IOkCommandHandler.Reason;

/**
 *
 * @author eric
 */
@Builder
@Data
public class ResponseHandlerImpl implements IResponseHandler {

    private ICommandHandler commandResponseHandler;

    @Override
    public void process(String message, Relay relay) throws NostrException {
        ArrayValue jsonArr = new JsonArrayUnmarshaller(message).unmarshall();
        final String command = (jsonArr).get(0).get().getValue().toString();

        switch (command) {
            case "EOSE" -> {
                var subId = (jsonArr).get(1).get().getValue().toString();
                commandResponseHandler.process(subId, relay);
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
                
                commandResponseHandler.process(eventId, reasonMessage, reason, result, relay);
            }
            case "NOTICE" -> {
                var param = jsonArr.get(1).get().getValue().toString();
                commandResponseHandler.process(param, relay);
            }
            case "EVENT" -> {
                var subId = jsonArr.get(1).get().getValue().toString();
                var jsonEvent = jsonArr.get(2).get().toString();

                commandResponseHandler.process(jsonEvent, subId, relay);
            }
            case "AUTH" -> {
                var challenge = jsonArr.get(1).get().getValue().toString();

                commandResponseHandler.process(challenge, relay);
            }
            default -> {
                throw new AssertionError();
            }
        }
    }
}
