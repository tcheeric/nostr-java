package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.BaseMessage;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
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
}
