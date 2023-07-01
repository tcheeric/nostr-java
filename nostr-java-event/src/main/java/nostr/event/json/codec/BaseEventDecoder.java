package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.BaseEvent;
import nostr.event.impl.GenericEvent;
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
            var mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            return mapper.readValue(jsonEvent, GenericEvent.class);
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }
    }
}
