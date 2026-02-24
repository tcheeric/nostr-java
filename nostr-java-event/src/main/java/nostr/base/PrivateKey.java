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
  }

  public PrivateKey(String hexPrivKey) {
    super(KeyType.PRIVATE, NostrUtil.hexToBytes(hexPrivKey), Bech32Prefix.NSEC);
  }

  /**
   * @return A strong pseudo random private key
   */
  public static PrivateKey generateRandomPrivKey() {
    return new PrivateKey(Schnorr.generatePrivateKey());
  }
}
