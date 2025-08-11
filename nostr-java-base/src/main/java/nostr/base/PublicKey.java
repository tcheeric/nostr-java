package nostr.base;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class PublicKey extends BaseKey {

    public PublicKey(byte[] rawData) {
        super(KeyType.PUBLIC, rawData, Bech32Prefix.NPUB);
        log.debug("Created public key from byte array");
    }

    public PublicKey(String hexPubKey) {
        super(KeyType.PUBLIC, NostrUtil.hexToBytes(hexPubKey), Bech32Prefix.NPUB);
        log.debug("Created public key from hex string");
    }
}
