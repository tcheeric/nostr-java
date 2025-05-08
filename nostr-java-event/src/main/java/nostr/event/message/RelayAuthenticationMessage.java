package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;

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
        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(
            JsonNodeFactory.instance.arrayNode()
                .add(getCommand())
                .add(getChallenge()));
    }

    public static <T extends BaseMessage> T decode(@NonNull Object arg) {
        return (T) new RelayAuthenticationMessage(arg.toString());
    }
}
