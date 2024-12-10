package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.FEncoder;
import nostr.base.GenericTagQuery;
import nostr.base.Relay;

/**
 * @author guilhermegps
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class GenericTagQueryEncoder<T extends GenericTagQuery> implements FEncoder<T> {

    private final GenericTagQuery genericTagQuery;
    private final Relay relay;

    public GenericTagQueryEncoder(GenericTagQuery tag) {
        this(tag, null);
    }

    @Override
    public String encode() {
        try {
            return FEncoder.MAPPER.writeValueAsString(genericTagQuery);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
