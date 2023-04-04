package nostr.event.marshaller.impl;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

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
    	var msgArray = new ArrayList<>();
        try {
	    	var mapper = getMapper();
	
	    	msgArray.add(message.getCommand());
	        if (message instanceof EventMessage msg) {
	        	JsonNode tree = mapper.readTree(new EventMarshaller(msg.getEvent(), relay, isEscape()).marshall());
	        	msgArray.add(tree);
	        } else if (message instanceof ReqMessage msg) {
	        	msgArray.add(msg.getSubscriptionId());
	        	JsonNode tree = mapper.readTree(new FiltersMarshaller(msg.getFilters(), relay).marshall());
	        	msgArray.add(tree);
	        } else if (message instanceof NoticeMessage msg) {
	        	msgArray.add(msg.getMessage());
	        } else if (message instanceof CloseMessage msg) {
	        	msgArray.add(msg.getSubscriptionId());
	        } else {
	            throw new NostrException(String.format("Invalid message type %s", message));
	        }
        
			return mapper.writeValueAsString(msgArray);
		} catch (JsonProcessingException e) {
			throw new NostrException(e);
		}
    }
}
