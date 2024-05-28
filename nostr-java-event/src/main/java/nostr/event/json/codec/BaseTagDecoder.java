package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.BaseTag;

/**
 *
 * @author eric
 */
@Data
public class BaseTagDecoder<T extends BaseTag> implements IDecoder<T> {

    private final Class<T> clazz;
    private final String jsonString;

    public BaseTagDecoder(String jsonString) {
        this.clazz = (Class<T>) BaseTag.class;
        this.jsonString = jsonString;
    }

    @Override
    public T decode() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
