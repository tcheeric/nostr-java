package nostr.event.json.codec;

import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.BaseTag;
import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.event.json.codec.EventEncodingException;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

/**
 *
 * @author eric
 */
@Data
public class BaseTagDecoder<T extends BaseTag> implements IDecoder<T> {

    private final Class<T> clazz;

    public BaseTagDecoder() {
        this.clazz = (Class<T>) BaseTag.class;
    }

    @Override
    /**
     * Decodes the provided JSON string into a tag instance.
     *
     * @param jsonString JSON representation of the tag
     * @return decoded tag
     * @throws EventEncodingException if decoding fails
     */
    public T decode(String jsonString) throws EventEncodingException {
        try {
            return MAPPER_BLACKBIRD.readValue(jsonString, clazz);
        } catch (JsonProcessingException ex) {
            throw new EventEncodingException("Failed to decode tag", ex);
        }
    }

}
