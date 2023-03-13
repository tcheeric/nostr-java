package nostr.base;

import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
public class PrivateKey extends BaseKey {

    public PrivateKey(byte[] rawData) {
        super(KeyType.PRIVATE, rawData, Bech32Prefix.NSEC);
    }

    public PrivateKey(String hexPrivKey) {
    	super(KeyType.PRIVATE, NostrUtil.hexToBytes(hexPrivKey), Bech32Prefix.NSEC);
    }

}
