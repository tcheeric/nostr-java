
package com.tcheeric.nostr.event.marshaller.impl;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.Relay;
import com.tcheeric.nostr.event.BaseMessage;
import com.tcheeric.nostr.event.marshaller.BaseMarshaller;
import com.tcheeric.nostr.event.message.CloseMessage;
import com.tcheeric.nostr.event.message.EventMessage;
import com.tcheeric.nostr.event.message.NoticeMessage;
import com.tcheeric.nostr.event.message.ReqMessage;
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
