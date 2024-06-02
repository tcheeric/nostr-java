
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
 * @author squirrel
 */
@Setter
@Getter
public class EoseMessage extends BaseMessage {

    @JsonProperty
    private final String subscriptionId;
    private EoseMessage() {
        this(null);
    }

    public EoseMessage(String subId) {
        super(Command.EOSE.name());
        this.subscriptionId = subId;
    }

    @Override
    public String encode() throws JsonProcessingException {
        return IEncoder.MAPPER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(getSubscriptionId()));
    }

    public static <T extends BaseMessage> T decode(@NonNull Object arg) {
        return (T) new EoseMessage(arg.toString());
    }
}
