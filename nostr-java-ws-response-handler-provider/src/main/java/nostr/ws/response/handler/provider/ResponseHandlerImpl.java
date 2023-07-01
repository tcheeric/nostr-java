/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nostr.ws.response.handler.provider;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.logging.Level;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.base.annotation.DefaultHandler;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.ClientAuthenticationMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.RelayAuthenticationMessage;
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
            log.log(Level.WARNING, "No custom command handler provided. Using default command handler...");
            try {
                this.commandHandler = ServiceLoader
                        .load(ICommandHandler.class).stream().map(p -> p.get())
                        .filter(ch -> ch.getClass().isAnnotationPresent(DefaultHandler.class))
                        .findFirst()
                        .get();
            } catch (NoSuchElementException e) {
                throw new AssertionError("Could not load the default handler", e);
            }
        }
    }

    @Override
    public void process(String message, Relay relay) throws NostrException {

        log.log(Level.INFO, "Process Message: {0} from relay: {1}", new Object[]{message, relay});

        var oMsg = new BaseMessageDecoder(message).decode();
        final String command = oMsg.getCommand();

        switch (command) {
            case "EOSE" -> {
                if (oMsg instanceof EoseMessage msg) {
                    commandHandler.onEose(msg.getSubscriptionId(), relay);
                } else {
                    throw new AssertionError("EOSE");
                }
            }
            case "OK" -> {
                if (oMsg instanceof OkMessage msg) {
                    String eventId = msg.getEventId();
                    boolean result = msg.getFlag();
                    String strMsg = msg.getMessage();
                    final var msgSplit = strMsg.split(":", 2);
                    Reason reason;
                    String reasonMessage = strMsg;
                    if (msgSplit.length < 2) {
                        reason = Reason.UNDEFINED;
                    } else {
                        reason = Reason.fromCode(msgSplit[0]).orElseThrow(RuntimeException::new);
                        reasonMessage = msgSplit[1];
                    }

                    commandHandler.onOk(eventId, reasonMessage, reason, result, relay);
                } else {
                    throw new AssertionError("OK");
                }
            }
            case "NOTICE" -> {
                if (oMsg instanceof NoticeMessage msg) {
                    commandHandler.onNotice(msg.getMessage());
                } else {
                    throw new AssertionError("NOTICE");
                }
            }
            case "EVENT" -> {
                if (oMsg instanceof EventMessage msg) {
                    var subId = msg.getSubscriptionId();
                    var jsonEvent = msg.getEvent().toString();
                    commandHandler.onEvent(jsonEvent, subId, relay);
                } else {
                    throw new AssertionError("EVENT");
                }
            }

            case "AUTH" -> {
                if (oMsg instanceof RelayAuthenticationMessage msg) {
                    var challenge = msg.getChallenge();
                    commandHandler.onAuth(challenge, relay);
                } else if (oMsg instanceof ClientAuthenticationMessage msg) {
                    // Actually, do nothing!
                } else {
                    throw new AssertionError("AUTH");
                }

            }
            default -> {
                throw new AssertionError("Unknown command " + command);
            }
        }
    }
}
