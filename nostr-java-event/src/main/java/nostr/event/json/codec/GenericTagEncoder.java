package nostr.event.json.codec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.base.IEncoder;
import nostr.base.Relay;
import nostr.event.impl.GenericTag;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class GenericTagEncoder implements IEncoder<GenericTag> {

    private final GenericTag tag;
    private final Relay relay;

    public GenericTagEncoder(@NonNull GenericTag tag) {
        this(tag, null);
    }
    
    @Override
    public String encode() throws NostrException {
        var encoder = new BaseTagEncoder(tag, relay);
        return encoder.encode();
    }    
}
