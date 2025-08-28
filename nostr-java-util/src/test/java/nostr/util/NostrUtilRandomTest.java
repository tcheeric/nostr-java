package nostr.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class NostrUtilRandomTest {

  @Test
  public void testCreateRandomByteArrayLength() {
    int len = 16;
    byte[] data = NostrUtil.createRandomByteArray(len);
    assertNotNull(data, "Random byte array should not be null");
    assertEquals(len, data.length, "Random byte array has wrong length");
  }

  @Test
  public void testCreateRandomByteArrayUniqueness() {
    byte[] data1 = NostrUtil.createRandomByteArray(16);
    byte[] data2 = NostrUtil.createRandomByteArray(16);
    assertFalse(Arrays.equals(data1, data2), "Consecutive random arrays should differ");
  }
}
