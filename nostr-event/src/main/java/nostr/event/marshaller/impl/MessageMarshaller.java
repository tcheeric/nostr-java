package nostr.event.marshaller.impl;

import java.util.logging.Level;

import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.event.impl.GenericMessage;
import nostr.event.marshaller.BaseElementMarshaller;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Log
public class MessageMarshaller extends BaseElementMarshaller {

    public MessageMarshaller(GenericMessage message, Relay relay) {
        this(message, relay, false);
    }

    public MessageMarshaller(GenericMessage baseMessage, Relay relay, boolean escape) {
        super(baseMessage, relay, escape);
    }

//    TODO: Improve
    @Override
    public String marshall() throws NostrException {
        GenericMessage message = (GenericMessage) getElement();
        Relay relay = getRelay();

        if (message instanceof EventMessage msg) {
            return "[\"" + msg.getCommand() + "\"," + new EventMarshaller(msg.getEvent(), relay, isEscape()).marshall() + "]";
        } else if (message instanceof ReqMessage msg) {
            return "[\"" + msg.getCommand() + "\",\"" + msg.getSubscriptionId() + "\"," + new FiltersMarshaller(msg.getFilters(), relay).marshall() + "]";
        } else if (message instanceof NoticeMessage msg) {
            return "[\"" + msg.getCommand() + "\",\"" + msg.getMessage() + "\"]";
        } else if (message instanceof CloseMessage msg) {
            return "[\"" + msg.getCommand() + "\",\"" + msg.getSubscriptionId() + "\"]";
        } else {
            log.log(Level.SEVERE, "Invalid message type {0}", message);
            throw new RuntimeException();
        }
    }
}
