package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.event.BaseMessage;

/**
 *
 * @author squirrel
 */
@Setter
@Getter
public class CloseMessage extends BaseMessage {

    @JsonProperty
    private final String subscriptionId;

    private CloseMessage() {
        this(null);
    }

    public CloseMessage(String subscriptionId) {
        super(Command.CLOSE.name());
        this.subscriptionId = subscriptionId;
    }

    @Override
    public String encode() throws JsonProcessingException {
        return IEncoder.MAPPER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(getSubscriptionId()));
    }
}
