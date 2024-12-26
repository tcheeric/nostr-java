package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import nostr.event.impl.Filters;

/**
 *
 * @author eric
 */
@Data
public class FiltersDecoder<T extends Filters> implements FDecoder<T> {
    private final Class<T> clazz = (Class<T>)Filters.class;

    @Override
    public T decode(@NonNull String jsonString)  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
