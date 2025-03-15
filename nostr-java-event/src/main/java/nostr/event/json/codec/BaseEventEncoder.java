package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.IEncoder;
import nostr.event.BaseEvent;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@Data
public class BaseEventEncoder<T extends BaseEvent> implements IEncoder<T> {

    private final T event;

    public BaseEventEncoder(T event) {
        this.event = event;
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
            return I_ENCODER_MAPPER_AFTERBURNER.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
