package nostr.event.message;

import static nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD;
import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.base.IEvent;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.EventEncodingException;

@Setter
@Getter
@Slf4j
public class EventMessage extends BaseMessage {
  private static final int SIZE_JSON_EVENT_wo_SIG_ID = 2;
  private static final Function<Object[], Boolean> isEventWoSig =
      (objArr) -> Objects.equals(SIZE_JSON_EVENT_wo_SIG_ID, objArr.length);

  @JsonProperty private final IEvent event;

  @JsonProperty private String subscriptionId;

  public EventMessage(@NonNull IEvent event) {
    super(Command.EVENT.name());
    this.event = event;
  }

  public EventMessage(@NonNull IEvent event, @NonNull String subscriptionId) {
    this(event);
    this.subscriptionId = subscriptionId;
  }

  @Override
  public String encode() throws EventEncodingException {
    var arrayNode = JsonNodeFactory.instance.arrayNode().add(getCommand());
    Optional.ofNullable(getSubscriptionId()).ifPresent(arrayNode::add);
    try {
      arrayNode.add(
          ENCODER_MAPPER_BLACKBIRD.readTree(
              new BaseEventEncoder<>((BaseEvent) getEvent()).encode()));
      return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(arrayNode);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode event message", e);
    }
  }

  public static <T extends BaseMessage> T decode(@NonNull String jsonString)
      throws EventEncodingException {
    try {
      Object[] msgArr = I_DECODER_MAPPER_BLACKBIRD.readValue(jsonString, Object[].class);
      return isEventWoSig.apply(msgArr) ? processEvent(msgArr[1]) : processEvent(msgArr);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Invalid argument: " + jsonString, e);
    }
  }

  // Generics are erased at runtime; BaseMessage subtype is determined by caller context
  @SuppressWarnings("unchecked")
  private static <T extends BaseMessage> T processEvent(Object o) {
    return (T) new EventMessage(convertValue((Map<String, String>) o));
  }

  // Generics are erased at runtime; BaseMessage subtype is determined by caller context
  @SuppressWarnings("unchecked")
  private static <T extends BaseMessage> T processEvent(Object[] msgArr) {
    return (T)
        new EventMessage(convertValue((Map<String, String>) msgArr[2]), msgArr[1].toString());
  }

  private static GenericEvent convertValue(Map<String, String> map) {
    log.info("Converting map to GenericEvent: {}", map);
    return I_DECODER_MAPPER_BLACKBIRD.convertValue(map, new TypeReference<>() {});
  }
}
