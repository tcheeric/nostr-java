package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import nostr.base.FDecoder;
import nostr.event.impl.Filters;

@Data
public class FiltersListDecoder<T extends Filters> implements FDecoder<T> {
    private final Class<T> clazz;
    private final String jsonString;

    public FiltersListDecoder(String jsonString) {
        this.clazz = (Class<T>) Filters.class;
        this.jsonString = jsonString;
    }

    public T decode() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
