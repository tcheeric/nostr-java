package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;

import static nostr.base.IEncoder.I_ENCODER_MAPPER_AFTERBURNER;

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
        return I_ENCODER_MAPPER_AFTERBURNER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(getEventId())
                .add(getFlag())
                .add(getMessage()));
    }

    //    TODO: refactor into stream returning optional
    public static <T extends BaseMessage> T decode(@NonNull Object[] msgArr) {
        if (msgArr.length == 4 && msgArr[2] instanceof Boolean duplicate) {
            String msgArg = msgArr[3].toString();
            return (T) new OkMessage(msgArr[1].toString(), duplicate, msgArg);
        } else {
            throw new AssertionError("Invalid argument: " + msgArr[2]);
        }
    }
}
