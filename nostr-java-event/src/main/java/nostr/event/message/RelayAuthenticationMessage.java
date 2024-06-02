package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.BaseMessage;

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

    public static <T extends BaseMessage> T decode(@NonNull Object arg) {
        return (T) new RelayAuthenticationMessage(arg.toString());
    }
}
