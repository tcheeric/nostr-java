package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.json.EventJsonMapper;
import nostr.event.json.codec.EventEncodingException;

import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

@Setter
@Getter
public class OkMessage extends BaseMessage {

  @JsonProperty private final String eventId;

  @JsonProperty private final Boolean flag;

  @JsonProperty private final String message;

  public OkMessage(String eventId, Boolean flag, String message) {
    super(Command.OK.name());
    this.eventId = eventId;
    this.flag = flag;
    this.message = message;
  }

  @Override
  public String encode() throws EventEncodingException {
    try {
      return EventJsonMapper.getMapper().writeValueAsString(
          JsonNodeFactory.instance
              .arrayNode()
              .add(getCommand())
              .add(getEventId())
              .add(getFlag())
              .add(getMessage()));
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode ok message", e);
    }
  }

  // Generics are erased at runtime; BaseMessage subtype is determined by caller context
  public static <T extends BaseMessage> T decode(@NonNull String jsonString)
      throws EventEncodingException {
    try {
      Object[] msgArr = I_DECODER_MAPPER_BLACKBIRD.readValue(jsonString, Object[].class);
      @SuppressWarnings("unchecked")
      T result = (T) new OkMessage(msgArr[1].toString(), (Boolean) msgArr[2], msgArr[3].toString());
      return result;
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to decode ok message", e);
    }
  }
}
