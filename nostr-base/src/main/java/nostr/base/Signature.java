package nostr.base;

import nostr.util.NostrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 *
 * @author squirrel
 */
@Data
@Builder
@AllArgsConstructor
public class Signature {
    
    @NonNull
    private final byte[] rawData;
    
    @NonNull
    private final PublicKey pubKey;
    
    @Override
    public String toString() {
        return NostrUtil.bytesToHex(rawData);
    }
}
