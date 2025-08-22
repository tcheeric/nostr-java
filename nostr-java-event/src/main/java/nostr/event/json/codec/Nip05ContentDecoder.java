package nostr.event.json.codec;

import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.Nip05Content;
import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.event.json.codec.EventEncodingException;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

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
    /**
     * Decodes a JSON representation of NIP-05 content.
     *
     * @param jsonContent JSON content string
     * @return decoded content
     * @throws EventEncodingException if decoding fails
     */
    public T decode(String jsonContent) throws EventEncodingException {
        try {
            return MAPPER_BLACKBIRD.readValue(jsonContent, clazz);
        } catch (JsonProcessingException ex) {
            throw new EventEncodingException("Failed to decode nip05 content", ex);
        }
    }
}
