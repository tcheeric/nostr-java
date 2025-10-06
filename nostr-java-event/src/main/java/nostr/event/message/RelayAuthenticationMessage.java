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
 * @author eric
 */
@Setter
@Getter
public class RelayAuthenticationMessage extends BaseAuthMessage {

  @JsonProperty private final String challenge;

  public RelayAuthenticationMessage(String challenge) {
    super(Command.AUTH.name());
    this.challenge = challenge;
  }

  @Override
  public String encode() throws EventEncodingException {
    try {
      return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(
          JsonNodeFactory.instance.arrayNode().add(getCommand()).add(getChallenge()));
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode relay authentication message", e);
    }
  }

  // Generics are erased at runtime; BaseMessage subtype is determined by caller context
  @SuppressWarnings("unchecked")
  public static <T extends BaseMessage> T decode(@NonNull Object arg) {
    return (T) new RelayAuthenticationMessage(arg.toString());
  }
}
