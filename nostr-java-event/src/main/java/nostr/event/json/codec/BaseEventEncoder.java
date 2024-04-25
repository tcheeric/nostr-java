package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IEncoder;
import nostr.base.Relay;
import nostr.event.BaseEvent;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Data
public class BaseEventEncoder implements IEncoder<BaseEvent> {

    private final BaseEvent event;
    private final Relay relay;

    protected BaseEventEncoder() {
        this.event = null;
        this.relay = null;
    }
    
    public BaseEventEncoder(BaseEvent event) {
        this(event, null);
    }

    @Override
    public String encode() {
        try {
            return toJson();
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String toJson() throws NostrException {
        try {
            return IEncoder.MAPPER.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
