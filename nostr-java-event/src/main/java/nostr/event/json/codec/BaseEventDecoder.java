package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.BaseEvent;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class BaseEventDecoder implements IDecoder<BaseEvent> {

    private final String jsonEvent;
    
    @Override
    public BaseEvent decode() throws NostrException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BaseEvent event = objectMapper.readValue(jsonEvent, BaseEvent.class);
            return event;
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }
    }
}
