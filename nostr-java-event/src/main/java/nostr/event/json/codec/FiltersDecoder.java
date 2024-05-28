package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.FDecoder;
import nostr.event.impl.Filters;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class FiltersDecoder<T extends Filters> implements FDecoder<T> {

    private final String jsonString;

    @Override
    public T decode(Class<T> clazz)  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
