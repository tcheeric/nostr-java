package nostr.event.marshaller.impl;

import nostr.base.Relay;
import nostr.event.marshaller.BaseElementMarshaller;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.event.impl.GenericMessage;
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

    @Override
    public String marshall() throws NostrException {
        GenericMessage message = (GenericMessage) getElement();
        Relay relay = getRelay();

        if (message instanceof EventMessage msg) {
            return "[\"" + msg.getCommand() + "\"," + new EventMarshaller(msg.getEvent(), relay, isEscape()).marshall() + "]";
        } else if (message instanceof ReqMessage msg) {
            return "[\"" + msg.getCommand() + "\",\"" + msg.getSubscriptionId() + "\"," + new FiltersListMarshaller(msg.getFiltersList(), relay).marshall() + "]";
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
