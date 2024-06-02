package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.BaseMessage;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.json.codec.BaseEventEncoder;

import java.util.Map;

/**
 *
 * @author eric
 */
@Setter
@Getter
public class CanonicalAuthenticationMessage extends BaseAuthMessage {

  @JsonProperty
  private final CanonicalAuthenticationEvent event;

  public CanonicalAuthenticationMessage(CanonicalAuthenticationEvent event) {
    super(Command.AUTH.name());
    this.event = event;
  }
  @Override
  public String encode() throws JsonProcessingException {
    return IEncoder.MAPPER.writeValueAsString(
        getArrayNode()
            .add(getCommand())
            .add(IEncoder.MAPPER.readTree(
                new BaseEventEncoder<>(getEvent()).encode())));
  }

  public static <T extends BaseMessage> T decode(@NonNull Map map, ObjectMapper mapper) {
    var event = mapper.convertValue(map, new TypeReference<CanonicalAuthenticationEvent>() {});
    return (T) new CanonicalAuthenticationMessage(event);
  }
}