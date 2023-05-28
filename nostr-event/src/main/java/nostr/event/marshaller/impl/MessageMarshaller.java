package nostr.event.marshaller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import nostr.base.Relay;
import nostr.event.impl.GenericMessage;
import nostr.event.message.AuthMessage;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class MessageMarshaller extends ElementMarshaller {

    public MessageMarshaller(GenericMessage baseMessage, Relay relay) {
        super(baseMessage, relay);
    }

    @Override
    public String marshall() throws NostrException {
        GenericMessage message = (GenericMessage) getElement();
        Relay relay = getRelay();
        var arrayNode = JsonNodeFactory.instance.arrayNode();
        try {
            arrayNode.add(message.getCommand());
            if (message instanceof EventMessage msg) {
                JsonNode tree = MAPPER.readTree(new ElementMarshaller(msg.getEvent(), relay).marshall());
                arrayNode.add(tree);
            } else if (message instanceof ReqMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
                JsonNode tree = MAPPER.readTree(new ElementMarshaller(msg.getFilters(), relay).marshall());
                arrayNode.add(tree);
            } else if (message instanceof NoticeMessage msg) {
                arrayNode.add(msg.getMessage());
            } else if (message instanceof CloseMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
            } else if (message instanceof AuthMessage msg) {
                arrayNode.add(msg.getEvent().toString());
            } else {
                throw new NostrException(String.format("Invalid message type %s", message));
            }

            return MAPPER.writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
