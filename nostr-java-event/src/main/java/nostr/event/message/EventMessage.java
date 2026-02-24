package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.EventJsonMapper;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.EventEncodingException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

@Setter
@Getter
@Slf4j
public class EventMessage extends BaseMessage {
  private static final int SIZE_JSON_EVENT_wo_SIG_ID = 2;
  private static final Function<Object[], Boolean> isEventWoSig =
      (objArr) -> Objects.equals(SIZE_JSON_EVENT_wo_SIG_ID, objArr.length);

  @JsonProperty private final GenericEvent event;

  @JsonProperty private String subscriptionId;

  public EventMessage(@NonNull GenericEvent event) {
    super(Command.EVENT.name());
    this.event = event;
  }

  public EventMessage(@NonNull GenericEvent event, @NonNull String subscriptionId) {
    this(event);
    this.subscriptionId = subscriptionId;
  }

  @Override
  public String encode() throws EventEncodingException {
    var arrayNode = JsonNodeFactory.instance.arrayNode().add(getCommand());
    Optional.ofNullable(getSubscriptionId()).ifPresent(arrayNode::add);
    try {
      arrayNode.add(
          EventJsonMapper.getMapper().readTree(
              new BaseEventEncoder<>(getEvent()).encode()));
      return EventJsonMapper.getMapper().writeValueAsString(arrayNode);
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

  private static <T extends BaseMessage> T processEvent(Object o) {
    @SuppressWarnings("unchecked")
    T result = (T) new EventMessage(convertValue((Map<?, ?>) o));
    return result;
  }

  private static <T extends BaseMessage> T processEvent(Object[] msgArr) {
    @SuppressWarnings("unchecked")
    T result = (T) new EventMessage(convertValue((Map<?, ?>) msgArr[2]), msgArr[1].toString());
    return result;
  }

  private static GenericEvent convertValue(Map<?, ?> map) {
    log.info("Converting map to GenericEvent: {}", map);
    return I_DECODER_MAPPER_BLACKBIRD.convertValue(map, new TypeReference<>() {});
  }
}
