package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.IEncoder;
import static nostr.base.IEncoder.MAPPER;
import nostr.base.Relay;
import nostr.base.GenericTagQuery;
import nostr.util.NostrException;

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
    public String encode() throws NostrException {
        try {
            return MAPPER.writeValueAsString(genericTagQuery);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
