package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import nostr.event.impl.Filters;

@Data
public class FiltersListDecoder implements FDecoder<Filters> {
    private final Class<Filters> clazz;
    private final String jsonString;

    public FiltersListDecoder(String jsonString) {
        this.clazz = Filters.class;
        this.jsonString = jsonString;
    }

    public Filters decode() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
