package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import nostr.base.Relay;
import nostr.event.impl.GenericMessage;
import nostr.event.message.ClientAuthenticationMessage;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.RelayAuthMessage;
import nostr.event.message.ReqMessage;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class MessageEncoder extends ElementEncoder {

    public MessageEncoder(GenericMessage baseMessage, Relay relay) {
        super(baseMessage, relay);
    }

    @Override
    public String encode() throws NostrException {
        GenericMessage message = (GenericMessage) getElement();
        Relay relay = getRelay();
        var arrayNode = JsonNodeFactory.instance.arrayNode();
        try {
            arrayNode.add(message.getCommand());
            if (message instanceof EventMessage msg) {
                JsonNode tree = MAPPER.readTree(new ElementEncoder(msg.getEvent(), relay).encode());
                arrayNode.add(tree);
            } else if (message instanceof ReqMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
                JsonNode tree = MAPPER.readTree(new ElementEncoder(msg.getFilters(), relay).encode());
                arrayNode.add(tree);
            } else if (message instanceof NoticeMessage msg) {
                arrayNode.add(msg.getMessage());
            } else if (message instanceof CloseMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
            } else if (message instanceof ClientAuthenticationMessage msg) {
                arrayNode.add(msg.getEvent().toString());
            } else if (message instanceof RelayAuthMessage msg){
                arrayNode.add(msg.getChallenge());
            } else {
                throw new NostrException(String.format("Invalid message type %s", message));
            }

            return MAPPER.writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
