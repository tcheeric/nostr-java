package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IDecoder;
import nostr.event.BaseTag;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class BaseTagDecoder implements IDecoder<BaseTag> {

    private final String jsonString;
    
    @Override
    public BaseTag decode() throws NostrException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, BaseTag.class);
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }
    }

}
