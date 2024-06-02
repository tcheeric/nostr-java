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
public class OkMessage extends BaseMessage {

    @JsonProperty
    private final String eventId;

    @JsonProperty
    private final Boolean flag;

    @JsonProperty
    private final String message;

    public OkMessage(String eventId, Boolean flag, String message) {
        super(Command.OK.name());
        this.eventId = eventId;
        this.flag = flag;
        this.message = message;
    }

    @Override
    public String encode() throws JsonProcessingException {
        return IEncoder.MAPPER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(getEventId())
                .add(getFlag())
                .add(getMessage()));
    }
}
