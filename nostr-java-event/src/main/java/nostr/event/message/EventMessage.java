package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;
import static nostr.base.IDecoder.I_DECODER_MAPPER_AFTERBURNER;

@Setter
@Getter
public class EventMessage extends BaseMessage {
    private static final int SIZE_JSON_EVENT_wo_SIG_ID = 2;
    private static final Function<Object[], Boolean> isEventWoSig = (objArr) ->
            Objects.equals(SIZE_JSON_EVENT_wo_SIG_ID, objArr.length);

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
        arrayNode.add(ENCODER_MAPPED_AFTERBURNER.readTree(
            new BaseEventEncoder<>((BaseEvent) getEvent()).encode()));
        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(arrayNode);
    }

    public static <T extends BaseMessage> T decode(@NonNull String jsonString) {
        try {
            Object[] msgArr = I_DECODER_MAPPER_AFTERBURNER.readValue(jsonString, Object[].class);
            return isEventWoSig.apply(msgArr) ? processEvent(msgArr[1]) : processEvent(msgArr);
        } catch (Exception e) {
            throw new AssertionError("Invalid argument: " + jsonString);
        }
    }

    public static <T extends BaseMessage> T processEvent(Object o) {
        return (T) new EventMessage(convertValue((Map<String, String>) o));
    }

    public static <T extends BaseMessage> T processEvent(Object[] msgArr) {
        return (T) new EventMessage(convertValue((Map<String, String>) msgArr[2]), msgArr[1].toString());
    }

    private static GenericEvent convertValue(Map<String, String> map) {
        return nostr.base.IDecoder.I_DECODER_MAPPER_AFTERBURNER.convertValue(map, new TypeReference<>() {});
    }
}
