package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;

import static nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD;
import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

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
        return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(
            JsonNodeFactory.instance.arrayNode()
                .add(getCommand())
                .add(getEventId())
                .add(getFlag())
                .add(getMessage()));
    }

    public static <T extends BaseMessage> T decode(@NonNull String jsonString) {
        try {
            Object[] msgArr = I_DECODER_MAPPER_BLACKBIRD.readValue(jsonString, Object[].class);
            return (T) new OkMessage(msgArr[1].toString(), (Boolean) msgArr[2], msgArr[3].toString());
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }
}
