package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.base.IEvent;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseEventEncoder;

@Setter
@Getter
public class EventMessage extends BaseMessage {

    @JsonProperty
    private final IEvent event;

    @JsonProperty
    private String subscriptionId;

    public EventMessage(@NonNull IEvent event) {
        this(event, null);
    }

    public EventMessage(@NonNull IEvent event, String subscriptionId) {
        super(Command.EVENT.name());
        this.event = event;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public String encode() throws JsonProcessingException {
        return IEncoder.MAPPER.writeValueAsString(
            getArrayNode()
                .add(getCommand())
                .add(IEncoder.MAPPER.readTree(
                    new BaseEventEncoder<>((BaseEvent)getEvent()).encode())));
    }
}
