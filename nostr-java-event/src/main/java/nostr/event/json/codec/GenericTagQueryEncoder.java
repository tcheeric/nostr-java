package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.GenericTagQuery;
import nostr.base.IEncoder;
import nostr.base.Relay;

/**
 * @author guilhermegps
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class GenericTagQueryEncoder implements IEncoder<GenericTagQuery> {

    private final GenericTagQuery genericTagQuery;
    private final Relay relay;

    public GenericTagQueryEncoder(GenericTagQuery tag) {
        this(tag, null);
    }

    @Override
    public String encode() {
        try {
            return IEncoder.MAPPER.writeValueAsString(genericTagQuery);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
