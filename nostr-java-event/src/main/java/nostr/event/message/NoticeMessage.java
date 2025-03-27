package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;

/**
 *
 * @author squirrel
 */
@Setter
@Getter
public class NoticeMessage extends BaseMessage {

    @JsonProperty
    private final String message;

    public NoticeMessage(@NonNull String message) {
        super(Command.NOTICE.name());
        this.message = message;
    }

    @Override
    public String encode() throws JsonProcessingException {
        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(getMessage()));
    }

    public static <T extends BaseMessage> T decode(@NonNull Object arg) {
        return (T) new NoticeMessage(arg.toString());
    }
}
