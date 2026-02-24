package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.EventJsonMapper;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.EventEncodingException;

import java.util.Map;

import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

/**
 * @author eric
 */
@Setter
@Getter
public class CanonicalAuthenticationMessage extends BaseMessage {

  @JsonProperty private final GenericEvent event;

  public CanonicalAuthenticationMessage(GenericEvent event) {
    super(Command.AUTH.name());
    this.event = event;
  }

  @Override
  public String encode() throws EventEncodingException {
    try {
      return EventJsonMapper.getMapper().writeValueAsString(
          JsonNodeFactory.instance
              .arrayNode()
              .add(getCommand())
              .add(EventJsonMapper.getMapper().readTree(new BaseEventEncoder<>(getEvent()).encode())));
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode canonical authentication message", e);
    }
  }

  public static <T extends BaseMessage> T decode(@NonNull Map<String, Object> map) {
    try {
      var event =
          I_DECODER_MAPPER_BLACKBIRD.convertValue(map, new TypeReference<GenericEvent>() {});

      @SuppressWarnings("unchecked")
      T result = (T) new CanonicalAuthenticationMessage(event);
      return result;
    } catch (IllegalArgumentException ex) {
      throw new EventEncodingException("Failed to decode canonical authentication message", ex);
    }
  }

}
