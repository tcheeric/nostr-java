package nostr.event.message;

import nostr.event.json.EventJsonMapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.json.codec.EventEncodingException;

/**
 * @author squirrel
 */
@Setter
@Getter
public class EoseMessage extends BaseMessage {

  @JsonProperty private final String subscriptionId;

  private EoseMessage() {
    this(null);
  }

  public EoseMessage(String subId) {
    super(Command.EOSE.name());
    this.subscriptionId = subId;
  }

  @Override
  public String encode() throws EventEncodingException {
    try {
      return EventJsonMapper.getMapper().writeValueAsString(
          JsonNodeFactory.instance.arrayNode().add(getCommand()).add(getSubscriptionId()));
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode eose message", e);
    }
  }

  // Generics are erased at runtime; BaseMessage subtype is determined by caller context
  @SuppressWarnings("unchecked")
  public static <T extends BaseMessage> T decode(@NonNull Object arg) {
    return (T) new EoseMessage(arg.toString());
  }
}
