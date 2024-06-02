package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEncoder;

/**
 *
 * @author eric
 */
@Setter
@Getter
public class RelayAuthenticationMessage extends BaseAuthMessage {

    @JsonProperty
    private final String challenge;

    public RelayAuthenticationMessage(String challenge) {
        super(Command.AUTH.name());
        this.challenge = challenge;
    }

    @Override
    public String encode() throws JsonProcessingException {
        return IEncoder.MAPPER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(getChallenge()));
    }
}
