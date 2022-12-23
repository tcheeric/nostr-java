package nostr.base;

import lombok.EqualsAndHashCode;

/**
 *
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = true)
public class PublicKey extends BaseKey {

    public PublicKey(byte[] rawData) {
        super(KeyType.PUBLIC, rawData, Bech32Prefix.NPUB);
    }    
    
}
