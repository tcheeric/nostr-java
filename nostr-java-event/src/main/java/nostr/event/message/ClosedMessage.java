package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;

import static nostr.base.IEncoder.I_ENCODER_MAPPER_AFTERBURNER;

@Setter
@Getter
public class ClosedMessage extends BaseMessage {

    @JsonProperty
    private final String subscriptionId;

    @JsonProperty
    private final String message;

    public ClosedMessage(@NonNull String subId, @NonNull String message) {
        super(Command.CLOSED.name());
        this.subscriptionId = subId;
        this.message = message;
    }

    @Override
    public String encode() throws JsonProcessingException {
        return I_ENCODER_MAPPER_AFTERBURNER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(getSubscriptionId()));
    }
}
