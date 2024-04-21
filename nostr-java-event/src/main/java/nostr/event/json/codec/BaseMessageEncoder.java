package nostr.event.json.codec;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.base.IEncoder;
import nostr.base.Relay;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericMessage;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.event.message.CloseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.event.message.ReqMessage;

import java.util.List;

/**
 * @author squirrel
 */
@Data
@AllArgsConstructor
public class BaseMessageEncoder implements IEncoder<BaseMessage> {

    private final BaseMessage message;
    private final Relay relay;

    public BaseMessageEncoder(@NonNull BaseMessage message) {
        this(message, null);
    }

    @Override
    public String encode() {
        var arrayNode = JsonNodeFactory.instance.arrayNode();
        try {
            arrayNode.add(message.getCommand());
            if (message instanceof EventMessage msg) {
                JsonNode tree = IEncoder.MAPPER.readTree(new BaseEventEncoder((BaseEvent) msg.getEvent(), relay).encode());
                arrayNode.add(tree);
            } else if (message instanceof ReqMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
                // Encode each filter individually and join them with a comma
                List<Filters> filtersList = msg.getFiltersList().getList();
                for (Filters f : filtersList) {
                    try {
                        FiltersEncoder filtersEncoder = new FiltersEncoder(f, relay);
                        var filterNode = MAPPER.readTree(filtersEncoder.encode());
                        arrayNode.add(filterNode);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (message instanceof NoticeMessage msg) {
                arrayNode.add(msg.getMessage());
            } else if (message instanceof CloseMessage msg) {
                arrayNode.add(msg.getSubscriptionId());
            } else if (message instanceof CanonicalAuthenticationMessage msg) {
                JsonNode tree = IEncoder.MAPPER.readTree(new BaseEventEncoder(msg.getEvent(), relay).encode());
                arrayNode.add(tree);
            } else if (message instanceof RelayAuthenticationMessage msg) {
                arrayNode.add(msg.getChallenge());
            } else if (message instanceof GenericMessage msg) {
                arrayNode.add(msg.getCommand());
                msg.getAttributes().stream().map(a -> a.getValue()).forEach(v -> arrayNode.add(v.toString()));
            } else {
                throw new RuntimeException(String.format("Invalid message type %s", message));
            }

            return IEncoder.MAPPER.writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
