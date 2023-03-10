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

    public PublicKey(String pubKey) {
    	super(KeyType.PUBLIC, NostrUtil.hexToBytes(pubKey), Bech32Prefix.NPUB);
    }    
    
}
