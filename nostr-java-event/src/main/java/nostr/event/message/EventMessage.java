package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;

import java.util.Map;
import java.util.Optional;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;

@Setter
@Getter
@Log
public class EventMessage extends GenericMessage {

    @JsonProperty
    private final GenericEvent event;

    @JsonProperty
    private String subscriptionId;

    public EventMessage(@NonNull GenericEvent event) {
        this(event, null);
    }

    public EventMessage(@NonNull GenericEvent event, String subscriptionId) {
        super(Command.EVENT.name());
        this.event = event;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public String encode() throws JsonProcessingException {
        var arrayNode = getArrayNode().add(getCommand());
        Optional.ofNullable(getSubscriptionId())
            .ifPresent(arrayNode::add);
        arrayNode.add(ENCODER_MAPPED_AFTERBURNER.readTree(
            new BaseEventEncoder<>((BaseEvent)getEvent()).encode()));
        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(arrayNode);
    }

//    TODO: refactor into stream returning optional
    public static <T extends BaseMessage> T decode(@NonNull Object[] msgArr, ObjectMapper mapper) {
        var arg = msgArr[1];
        if (msgArr.length == 2 && arg instanceof Map map) {
            //log.log(Level.INFO, ">>> MsgArr {0}", msgArr[1]);
            return (T) new EventMessage(
                convertValue(mapper, map)
            );
        }

        if (msgArr.length == 3 && arg instanceof String) {
            if (msgArr[2] instanceof Map map) {
                //log.log(Level.INFO, ">>> MsgArr {0}", msgArr[2]);
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
