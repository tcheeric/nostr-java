package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.GenericTagQuery;
import static nostr.base.IEncoder.MAPPER;
import nostr.base.Relay;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericTagEncoder extends ElementEncoder {

    public GenericTagEncoder(GenericTagQuery tag, Relay relay) {
        super(tag, relay);
    }

    public GenericTagEncoder(GenericTagQuery tag) {
        super(tag);
    }

    @Override
    public String encode() throws NostrException {
        return toJson();
    }

    @Override
    protected String toJson() throws NostrException {
        try {
            return MAPPER.writeValueAsString((GenericTagQuery) getElement());
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }

}
