package nostr.base;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import nostr.base.util.PublicKeyDeserializer;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = PublicKeyDeserializer.class)
public class PublicKey extends BaseKey {

    public PublicKey(byte[] rawData) {
        super(KeyType.PUBLIC, rawData, Bech32Prefix.NPUB);
    }

    public PublicKey(String hexPubKey) {
    	super(KeyType.PUBLIC, NostrUtil.hexToBytes(hexPubKey), Bech32Prefix.NPUB);
    }    
}
