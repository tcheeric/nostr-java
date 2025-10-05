package nostr.base;

import lombok.extern.slf4j.Slf4j;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.crypto.schnorr.Schnorr;
import nostr.util.NostrUtil;

/**
 * @author squirrel
 */
@Slf4j
public class PrivateKey extends BaseKey {

  public PrivateKey(byte[] rawData) {
    super(KeyType.PRIVATE, rawData, Bech32Prefix.NSEC);
    log.debug("Created private key from byte array");
  }

  public PrivateKey(String hexPrivKey) {
    super(KeyType.PRIVATE, NostrUtil.hexToBytes(hexPrivKey), Bech32Prefix.NSEC);
    log.debug("Created private key from hex string");
  }

  /**
   * @return A strong pseudo random private key
   */
  public static PrivateKey generateRandomPrivKey() {
    PrivateKey key = new PrivateKey(Schnorr.generatePrivateKey());
    log.debug("Generated new random private key");
    return key;
  }
}
