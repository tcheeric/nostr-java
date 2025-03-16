package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.Nip05Content;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 *
 * @author eric
 */
@Data
public class Nip05ContentDecoder<T extends Nip05Content> implements IDecoder<T> {

    private final Class<T> clazz;

    public Nip05ContentDecoder() {
        this.clazz = (Class<T>) Nip05Content.class;
    }

    @Override
    public T decode(String jsonContent) {
        try {
            return MAPPER_AFTERBURNER.readValue(jsonContent, clazz);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
