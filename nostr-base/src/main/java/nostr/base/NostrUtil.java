
package nostr.base;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.NonNull;

import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
public class NostrUtil {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] b) {
        char[] hexChars = new char[b.length * 2];
        for (int j = 0; j < b.length; j++) {
            int v = b[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] buf = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            buf[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return buf;
    }

    public static byte[] bytesFromInt(int n) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(n).array();
    }

    public static byte[] bytesFromBigInteger(BigInteger n) {

        byte[] b = n.toByteArray();

        if (b.length == 32) {
            return b;
        } else if (b.length > 32) {
            return Arrays.copyOfRange(b, b.length - 32, b.length);
        } else {
            byte[] buf = new byte[32];
            System.arraycopy(b, 0, buf, buf.length - b.length, b.length);
            return buf;
        }
    }

    public static BigInteger bigIntFromBytes(byte[] b) {
        return new BigInteger(1, b);
    }

    public static byte[] sha256(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(b);
    }

    public static byte[] xor(byte[] b0, byte[] b1) {

        if (b0.length != b1.length) {
            return null;
        }

        byte[] ret = new byte[b0.length];
        int i = 0;
        for (byte b : b0) {
            ret[i] = (byte) (b ^ b1[i]);
            i++;
        }

        return ret;
    }

    public static byte[] createRandomByteArray(int len) {
        byte[] b = new byte[len];
        new Random().nextBytes(b);
        return b;
    }

    public static String supportedNips(@NonNull Relay relay) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        final List<Integer> supportedNipList = relay.getSupportedNips();

        sb.append("[");
        for (int n : supportedNipList) {

            sb.append(n);

            if (i++ < supportedNipList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");

        return sb.toString();
    }

}
