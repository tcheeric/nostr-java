package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.Nip05Content;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class Nip05ContentDecoder implements IDecoder<Nip05Content> {

    private final String jsonContent;

    @Override
    public Nip05Content decode() throws NostrException {
        try {
            return new ObjectMapper().readValue(this.jsonContent, Nip05Content.class);
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }
    }
}
