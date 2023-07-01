package nostr.event.json.codec;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IEncoder;
import static nostr.base.IEncoder.MAPPER;
import nostr.base.Relay;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericMessage;
import nostr.event.message.ClientAuthenticationMessage;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.event.message.ReqMessage;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@AllArgsConstructor
public class BaseMessageEncoder implements IEncoder<BaseMessage> {

    private final BaseMessage message;
    private final Relay relay;
    
    @Override
    public String encode() throws NostrException {
        var arrayNode = JsonNodeFactory.instance.arrayNode();
        try {
            arrayNode.add(message.getCommand());
            if (message instanceof EventMessage msg) {
                JsonNode tree = MAPPER.readTree(new BaseEventEncoder((BaseEvent) msg.getEvent(), relay).encode());
                arrayNode.add(tree);
            } else if (message instanceof ReqMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
                JsonNode tree = MAPPER.readTree(new BaseEventEncoder(msg.getFilters(), relay).encode());
                arrayNode.add(tree);
            } else if (message instanceof NoticeMessage msg) {
                arrayNode.add(msg.getMessage());
            } else if (message instanceof CloseMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
            } else if (message instanceof ClientAuthenticationMessage msg) {
                arrayNode.add(msg.getEvent().toString());
            } else if (message instanceof RelayAuthenticationMessage msg){
                arrayNode.add(msg.getChallenge());
            } else if (message instanceof GenericMessage msg) {
                arrayNode.add(msg.getCommand());
                msg.getAttributes().stream().map(a -> a.getValue()).forEach(v -> arrayNode.add(v.toString()));
            } else {
                throw new NostrException(String.format("Invalid message type %s", message));
            }

            return MAPPER.writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
