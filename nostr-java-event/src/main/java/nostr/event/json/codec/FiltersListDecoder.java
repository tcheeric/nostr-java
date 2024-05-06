package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.list.FiltersList;

@Data
@AllArgsConstructor
public class FiltersListDecoder implements IDecoder<FiltersList> {

    private final String jsonString;

    @Override
    public FiltersList decode() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, FiltersList.class);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
