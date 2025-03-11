package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEvent;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;

import java.util.Map;
import java.util.Optional;

import static nostr.base.IEncoder.I_ENCODER_MAPPER_AFTERBURNER;

@Setter
@Getter
public class EventMessage extends BaseMessage {

    @JsonProperty
    private final IEvent event;

    @JsonProperty
    private String subscriptionId;

    public EventMessage(@NonNull IEvent event) {
        this(event, null);
    }

    public EventMessage(@NonNull IEvent event, String subscriptionId) {
        super(Command.EVENT.name());
        this.event = event;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public String encode() throws JsonProcessingException {
        var arrayNode = getArrayNode().add(getCommand());
        Optional.ofNullable(getSubscriptionId())
            .ifPresent(arrayNode::add);
        arrayNode.add(I_ENCODER_MAPPER_AFTERBURNER.readTree(
            new BaseEventEncoder<>((BaseEvent)getEvent()).encode()));
        return I_ENCODER_MAPPER_AFTERBURNER.writeValueAsString(arrayNode);
    }

    public static <T extends BaseMessage> T decode(@NonNull Object[] msgArr, ObjectMapper mapper) {
        var arg = msgArr[1];
        if (msgArr.length == 2 && arg instanceof Map map) {
            return (T) new EventMessage(
                convertValue(mapper, map)
            );
        }

        if (msgArr.length == 3 && arg instanceof String) {
            if (msgArr[2] instanceof Map map) {
                return (T) new EventMessage(
                    convertValue(mapper, map),
                    arg.toString()
                );
            }
        }

        throw new AssertionError("Invalid argument: " + arg);
    }

    private static GenericEvent convertValue(ObjectMapper mapper, Map map) {
        return mapper.convertValue(map, new TypeReference<GenericEvent>() {
        });
    }
}
