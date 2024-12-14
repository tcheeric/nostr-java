package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.base.IEvent;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;

import java.util.Map;
import java.util.Optional;

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
        Optional.ofNullable(getSubscriptionId())
            .ifPresent(subscriptionId -> getArrayNode().add(subscriptionId));
        return IEncoder.MAPPER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(IEncoder.MAPPER.readTree(
                    new BaseEventEncoder<>((BaseEvent)getEvent()).encode())));
    }

    public static <T extends BaseMessage> T decode(@NonNull Object[] msgArr, ObjectMapper mapper) {
        var arg = msgArr[1];
        if (msgArr.length == 2 && arg instanceof Map map) {
            var event = mapper.convertValue(map, new TypeReference<GenericEvent>() {});
            return (T) new EventMessage(event);
        } else if (msgArr.length == 3 && arg instanceof String) {
            var subId = arg.toString();
            if (msgArr[2] instanceof Map map) {
                var event = mapper.convertValue(map, new TypeReference<GenericEvent>() {});
                return (T) new EventMessage(event, subId);
            }
        }
        throw new AssertionError("Invalid argument: " + arg);
    }
}
