package nostr.util;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class NostrUtilExtendedTest {

    @Test
    public void testBytesFromIntAndBigInteger() {
        int value = 0x12345678;
        byte[] bytes = NostrUtil.bytesFromInt(value);
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78}, bytes);

        BigInteger big = new BigInteger(1, new byte[]{0x01, 0x02});
        byte[] bigBytes = NostrUtil.bytesFromBigInteger(big);
        assertEquals(32, bigBytes.length);
        assertTrue(Arrays.equals(Arrays.copyOfRange(bigBytes, 30, 32), new byte[]{0x01, 0x02}));

        assertEquals(big, NostrUtil.bigIntFromBytes(bigBytes));
    }

    @Test
    public void testSha256AndXor() throws NoSuchAlgorithmException {
        byte[] data = new byte[]{1,2,3};
        byte[] hash = NostrUtil.sha256(data);
        assertEquals(32, hash.length);

        byte[] xored = NostrUtil.xor(data, new byte[]{3,2,1});
        assertArrayEquals(new byte[]{2,0,2}, xored);

        assertNull(NostrUtil.xor(new byte[]{1}, new byte[]{1,2}));
    }

    @Test
    public void testJsonEscapeAndUnescape() {
        String json = "{\"key\":\n\t'\\value'\r}";
        String escaped = NostrUtil.escapeJsonString(json);
        String unescaped = NostrUtil.unEscapeJsonString(escaped);
        assertEquals(json, unescaped);
    }

    @Test
    public void testHexLengthConversions() {
        String hex128 = "a".repeat(128);
        assertEquals(64, NostrUtil.hex128ToBytes(hex128).length);

        String nip04 = "a".repeat(66);
        assertEquals(33, NostrUtil.nip04PubKeyHexToBytes(nip04).length);
    }
}
