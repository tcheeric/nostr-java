package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author eric
 */
@Data
public class GenericEventDecoder<T extends GenericEvent> implements IDecoder<T> {

    private final Class<T> clazz;
    private final String jsonEvent;

    public GenericEventDecoder(String jsonEvent) {
        this.clazz = (Class<T>) GenericEvent.class;
        this.jsonEvent = jsonEvent;
    }
    @Override
    public T decode() {
        try {
            var mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            return mapper.readValue(jsonEvent, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
