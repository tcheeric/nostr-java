package nostr.test.base;

import lombok.extern.java.Log;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author squirrel
 */
@Log
public class NostrUtilTest {
  /**
   * test intended to confirm conversion routines:
   *    (1) Hex string to byte[], then
   *    (2) btye[] back to Hex string
   * are properly functioning inversions of each other
   */
  @Test
  public void testHexToBytesHex() throws NostrException {
    System.out.println("testBech32HexToBytesToBech32");
    String pubKeyString = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
    assertEquals(
        pubKeyString,
        NostrUtil.bytesToHex( // (2)
            NostrUtil.hexToBytes( // (1)
                pubKeyString)));
  }
}
