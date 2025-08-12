package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.BaseTag;

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
    public T decode(String jsonString) {
        try {
            return MAPPER_BLACKBIRD.readValue(jsonString, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
