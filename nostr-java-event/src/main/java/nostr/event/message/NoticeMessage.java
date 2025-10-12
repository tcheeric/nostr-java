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

/**
 * @author squirrel
 */
@Setter
@Getter
public class NoticeMessage extends BaseMessage {

  @JsonProperty private final String message;

  public NoticeMessage(@NonNull String message) {
    super(Command.NOTICE.name());
    this.message = message;
  }

  @Override
  public String encode() throws EventEncodingException {
    try {
      return EventJsonMapper.getMapper().writeValueAsString(
          JsonNodeFactory.instance.arrayNode().add(getCommand()).add(getMessage()));
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode notice message", e);
    }
  }

  // Generics are erased at runtime; BaseMessage subtype is determined by caller context
  public static <T extends BaseMessage> T decode(@NonNull Object arg) {
    @SuppressWarnings("unchecked")
    T result = (T) new NoticeMessage(arg.toString());
    return result;
  }
}
