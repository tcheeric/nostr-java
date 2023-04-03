package nostr.base;

import lombok.EqualsAndHashCode;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = true)
public class PublicKey extends BaseKey {

    public PublicKey(byte[] rawData) {
        super(KeyType.PUBLIC, rawData, Bech32Prefix.NPUB);
    }

    public PublicKey(String hexPubKey) {
    	super(KeyType.PUBLIC, NostrUtil.hexToBytes(hexPubKey), Bech32Prefix.NPUB);
    }
    
}
