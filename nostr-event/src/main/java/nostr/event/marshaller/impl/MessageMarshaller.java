
package nostr.event.marshaller.impl;

import nostr.base.NostrException;
import nostr.base.Relay;
import nostr.event.BaseMessage;
import nostr.event.marshaller.BaseMarshaller;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class MessageMarshaller extends BaseMarshaller {

    public MessageMarshaller(BaseMessage message, Relay relay) {
        this(message, relay, false);
    }

    public MessageMarshaller(BaseMessage baseMessage, Relay relay, boolean escape) {
        super(baseMessage, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        BaseMessage message = (BaseMessage) getElement();
        Relay relay = getRelay();

        if (message instanceof EventMessage) {
            EventMessage msg = (EventMessage) message;
            return "[\"" + msg.getCommand().name() + "\"," + new EventMarshaller(msg.getEvent(), relay, isEscape()).marshall() + "]";
        } else if (message instanceof ReqMessage) {
            ReqMessage msg = (ReqMessage) message;
            return "[\"" + msg.getCommand().name() + "\",\"" + msg.getSubscriptionId() + "\"," + new EventMarshaller(msg.getFilters(), relay, isEscape()).marshall() + "]";
        } else if (message instanceof NoticeMessage) {
            NoticeMessage msg = (NoticeMessage) message;
            return "[\"" + msg.getCommand().name() + "\",\"" + msg.getMessage() + "\"]";
        } else if (message instanceof CloseMessage) {
            CloseMessage msg = (CloseMessage) message;
            return "[\"" + msg.getCommand().name() + "\",\"" + msg.getSubscriptionId() + "\"]";
        } else {
            log.log(Level.SEVERE, "Invalid message type {0}", message);
            throw new RuntimeException();
        }
    }
}
