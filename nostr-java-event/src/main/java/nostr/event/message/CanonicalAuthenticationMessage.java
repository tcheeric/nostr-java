package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.json.codec.BaseEventEncoder;

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
}