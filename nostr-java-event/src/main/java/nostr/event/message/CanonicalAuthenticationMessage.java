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
import nostr.event.BaseTag;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.EventJsonMapper;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.EventEncodingException;
import nostr.event.tag.GenericTag;

import java.util.List;
import java.util.Map;

import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

/**
 * @author eric
 */
@Setter
@Getter
public class CanonicalAuthenticationMessage extends BaseAuthMessage {

  @JsonProperty private final CanonicalAuthenticationEvent event;

  public CanonicalAuthenticationMessage(CanonicalAuthenticationEvent event) {
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

  /**
   * Decodes a map representation into a CanonicalAuthenticationMessage.
   *
   * <p>This method converts the map (typically from JSON deserialization) into
   * a properly typed CanonicalAuthenticationMessage with a CanonicalAuthenticationEvent.
   *
   * @param map the map containing event data
   * @param <T> the message type (must be BaseMessage)
   * @return the decoded CanonicalAuthenticationMessage
   * @throws EventEncodingException if decoding fails
   */
  public static <T extends BaseMessage> T decode(@NonNull Map<String, Object> map) {
    try {
      var event =
          I_DECODER_MAPPER_BLACKBIRD.convertValue(map, new TypeReference<GenericEvent>() {});

      List<BaseTag> baseTags = event.getTags().stream().filter(GenericTag.class::isInstance).toList();

      CanonicalAuthenticationEvent canonEvent =
          new CanonicalAuthenticationEvent(event.getPubKey(), baseTags, "");

      canonEvent.setId(String.valueOf(map.get("id")));

      @SuppressWarnings("unchecked")
      T result = (T) new CanonicalAuthenticationMessage(canonEvent);
      return result;
    } catch (IllegalArgumentException ex) {
      throw new EventEncodingException("Failed to decode canonical authentication message", ex);
    }
  }

}
