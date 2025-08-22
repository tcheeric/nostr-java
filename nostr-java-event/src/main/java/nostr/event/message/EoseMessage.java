package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.json.codec.EventEncodingException;

import static nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD;

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
    public String encode() throws EventEncodingException {
        try {
            return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(
                JsonNodeFactory.instance.arrayNode()
                    .add(getCommand())
                    .add(getSubscriptionId()));
        } catch (JsonProcessingException e) {
            throw new EventEncodingException("Failed to encode eose message", e);
        }
    }

    public static <T extends BaseMessage> T decode(@NonNull Object arg) {
        return (T) new EoseMessage(arg.toString());
    }
}
