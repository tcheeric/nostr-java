package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import nostr.base.ElementAttribute;
import nostr.base.IDecoder;
import nostr.event.impl.GenericTag;

@Data
public class GenericTagDecoder<T extends GenericTag> implements IDecoder<T> {

    private final Class<T> clazz;
    private final String json;

    public GenericTagDecoder(String json) {
        this.clazz = (Class<T>) GenericTag.class;
        this.json = json;
    }

    @Override
    public T decode() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String[] jsonElements = objectMapper.readValue(this.json, String[].class);

            String code = jsonElements[0];

            List<ElementAttribute> attributes = new ArrayList<>();
            for (int i = 1; i < jsonElements.length; i++) {
                ElementAttribute attribute = new ElementAttribute("param"+(i-1), jsonElements[i], null);
                if (!attributes.contains(attribute)) {
                    attributes.add(attribute);
                }
            }

            return (T) new GenericTag(code, null, attributes);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
