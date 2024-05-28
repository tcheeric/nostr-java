package nostr.event.json.codec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.base.IEncoder;
import nostr.base.Relay;
import nostr.event.impl.GenericTag;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class GenericTagEncoder<T extends GenericTag> implements IEncoder<T> {

    private final GenericTag tag;
    private final Relay relay;

    public GenericTagEncoder(@NonNull GenericTag tag) {
        this(tag, null);
    }
    
    @Override
    public String encode() {
        var encoder = new BaseTagEncoder(tag, relay);
        return encoder.encode();
    }    
}
