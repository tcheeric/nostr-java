package nostr.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author squirrel
 */
@Slf4j
public class NostrUtilTest {
  /**
   * test intended to confirm conversion routines:
   *    (1) Hex string to byte[], then
   *    (2) byte[] back to Hex string
   * are properly functioning inversions of each other
   */
  @Test
  public void testHexToBytesHex() {
    log.info("testHexToBytesHex");
    String pubKeyString = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
    assertEquals(
        pubKeyString,
        NostrUtil.bytesToHex( // (2)
            NostrUtil.hexToBytes( // (1)
                pubKeyString)));
  }
}
