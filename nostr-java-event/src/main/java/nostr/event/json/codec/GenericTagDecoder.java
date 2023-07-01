package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.ElementAttribute;
import nostr.base.IDecoder;
import nostr.event.impl.GenericTag;
import nostr.util.NostrException;

@Data
@AllArgsConstructor
public class GenericTagDecoder implements IDecoder<GenericTag> {

    private final String json;

    @Override
    public GenericTag decode() throws NostrException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String[] jsonElements = objectMapper.readValue(this.json, String[].class);

            String code = jsonElements[0];

            Set<ElementAttribute> attributes = new HashSet<>();
            for (int i = 1; i < jsonElements.length; i++) {
                ElementAttribute attribute = new ElementAttribute(null, jsonElements[i], null);
                attributes.add(attribute);
            }

            return new GenericTag(code, null, attributes);
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }
    }
}
