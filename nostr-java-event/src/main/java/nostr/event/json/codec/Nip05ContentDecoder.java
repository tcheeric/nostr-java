package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.Nip05Content;

/**
 *
 * @author eric
 */
@Data
public class Nip05ContentDecoder<T extends Nip05Content> implements IDecoder<T> {

    private final Class<T> clazz;
    private final String jsonContent;

    public Nip05ContentDecoder(String jsonContent) {
        this.clazz = (Class<T>) Nip05Content.class;
        this.jsonContent = jsonContent;
    }

    @Override
    public T decode() {
        try {
            return new ObjectMapper().readValue(this.jsonContent, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
