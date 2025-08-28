package nostr.event.message;

import static nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD;

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
public class CloseMessage extends BaseMessage {

  @JsonProperty private final String subscriptionId;

  private CloseMessage() {
    this(null);
  }

  public CloseMessage(String subscriptionId) {
    super(Command.CLOSE.name());
    this.subscriptionId = subscriptionId;
  }

  @Override
  public String encode() throws EventEncodingException {
    try {
      return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(
          JsonNodeFactory.instance.arrayNode().add(getCommand()).add(getSubscriptionId()));
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode close message", e);
    }
  }

  public static <T extends BaseMessage> T decode(@NonNull Object arg) {
    return (T) new CloseMessage(arg.toString());
  }
}
