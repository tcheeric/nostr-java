package nostr.crypto.bech32;

import nostr.util.NostrUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Bech32 and Bech32m encoding/decoding implementation for NIP-19.
 *
 * <p>This class provides utilities for encoding and decoding Nostr identifiers using the Bech32
 * format defined in NIP-19. Bech32 provides human-readable, error-detecting encoding for binary
 * data, originally defined for Bitcoin addresses.
 *
 * <h2>What is Bech32?</h2>
 *
 * <p>Bech32 is an encoding scheme that:
 * <ul>
 *   <li>Uses a human-readable prefix (HRP) like "npub", "nsec", "note"</li>
 *   <li>Encodes binary data in a 32-character alphabet (no 0/O, 1/I/l confusion)</li>
 *   <li>Includes a 6-character checksum for error detection</li>
 *   <li>Is case-insensitive (always lowercase by convention)</li>
 *   <li>Uses separator '1' between HRP and data</li>
 * </ul>
 *
 * <p>Format: <code>[hrp]1[data][checksum]</code>
 *
 * <h2>Bech32 vs Bech32m</h2>
 *
 * <p>Two variants exist:
 * <ul>
 *   <li><strong>Bech32 (BIP-173):</strong> Original spec, used for simple data (npub, nsec, note)</li>
 *   <li><strong>Bech32m (BIP-350):</strong> Updated spec, used for TLV-encoded data (nprofile, nevent)</li>
 * </ul>
 *
 * <p>The difference is in the checksum constant used during encoding/decoding.
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Encode a Public Key (npub)</h3>
 * <pre>{@code
 * String hexPubKey = "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d";
 * String npub = Bech32.toBech32(Bech32Prefix.NPUB, hexPubKey);
 * // Returns: "npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"
 * }</pre>
 *
 * <h3>Example 2: Decode an npub Back to Hex</h3>
 * <pre>{@code
 * String npub = "npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6";
 * String hex = Bech32.fromBech32(npub);
 * // Returns: "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"
 * }</pre>
 *
 * <h3>Example 3: Low-Level Encoding</h3>
 * <pre>{@code
 * byte[] data = hexToBytes("3bf0c63f...");
 * byte[] fiveBitData = convertBits(data, 8, 5, true);
 * String encoded = Bech32.encode(Bech32.Encoding.BECH32, "npub", fiveBitData);
 * }</pre>
 *
 * <h3>Example 4: Low-Level Decoding</h3>
 * <pre>{@code
 * Bech32Data decoded = Bech32.decode("npub180cvv07tjdrrgpa0j...");
 * String hrp = decoded.hrp; // "npub"
 * byte[] fiveBitData = decoded.data;
 * Encoding encoding = decoded.encoding; // BECH32 or BECH32M
 * }</pre>
 *
 * <h2>Character Set</h2>
 *
 * <p>Bech32 uses a 32-character alphabet: <code>qpzry9x8gf2tvdw0s3jn54khce6mua7l</code>
 *
 * <p>This alphabet excludes:
 * <ul>
 *   <li><strong>1, b, i, o:</strong> Visually similar to other characters</li>
 *   <li><strong>Uppercase:</strong> All strings are lowercase</li>
 * </ul>
 *
 * <h2>Error Detection</h2>
 *
 * <p>Bech32 detects:
 * <ul>
 *   <li>Any single character error</li>
 *   <li>Any two adjacent character swaps</li>
 *   <li>Most insertion/deletion errors</li>
 *   <li>Most multi-character errors</li>
 * </ul>
 *
 * <h2>API Methods</h2>
 *
 * <table border="1">
 *   <tr>
 *     <th>Method</th>
 *     <th>Purpose</th>
 *     <th>Use Case</th>
 *   </tr>
 *   <tr>
 *     <td>{@link #toBech32(Bech32Prefix, String)}</td>
 *     <td>Hex string → Bech32</td>
 *     <td>Most common: encode keys/IDs</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #toBech32(Bech32Prefix, byte[])}</td>
 *     <td>Bytes → Bech32</td>
 *     <td>Encode raw binary data</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #fromBech32(String)}</td>
 *     <td>Bech32 → Hex string</td>
 *     <td>Decode to hex for processing</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #encode(Encoding, String, byte[])}</td>
 *     <td>Low-level encoding</td>
 *     <td>Custom encoding with 5-bit data</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #decode(String)}</td>
 *     <td>Low-level decoding</td>
 *     <td>Parse and validate Bech32</td>
 *   </tr>
 * </table>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>All methods are static and thread-safe.
 *
 * <h2>Exceptions</h2>
 *
 * <ul>
 *   <li><strong>IllegalArgumentException:</strong> Invalid input data</li>
 *   <li><strong>Bech32EncodingException:</strong> Encoding failures (wraps other exceptions)</li>
 *   <li><strong>Exception:</strong> Decoding errors (malformed input, invalid checksum, etc.)</li>
 * </ul>
 *
 * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki">BIP-173 (Bech32)</a>
 * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki">BIP-350 (Bech32m)</a>
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/19.md">NIP-19 Specification</a>
 * @see Bech32Prefix
 * @since 0.1.0
 */
public class Bech32 {

  /** The Bech32 character set for encoding. */
  private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

  /** The Bech32 character set for decoding. */
  private static final byte[] CHARSET_REV = {
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    15, -1, 10, 17, 21, 20, 26, 30, 7, 5, -1, -1, -1, -1, -1, -1,
    -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1,
    1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1,
    -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1,
    1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1
  };

  private static final int BECH32_CONST = 1;
  private static final int BECH32M_CONST = 0x2bc830a3;

  public enum Encoding {
    BECH32,
    BECH32M
  }

  public static class Bech32Data {

    public final Encoding encoding;
    public final String hrp;
    public final byte[] data;

    private Bech32Data(final Encoding encoding, final String hrp, final byte[] data) {
      this.encoding = encoding;
      this.hrp = hrp;
      this.data = data;
    }
  }

  public static String toBech32(Bech32Prefix hrp, byte[] hexKey) {
    try {
      byte[] data = convertBits(hexKey, 8, 5, true);
      return Bech32.encode(Bech32.Encoding.BECH32, hrp.getCode(), data);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new Bech32EncodingException("Failed to encode key to Bech32", e);
    }
  }

  public static String toBech32(Bech32Prefix hrp, String hexKey) {
    byte[] data = NostrUtil.hexToBytes(hexKey);

    return toBech32(hrp, data);
  }

  // Added by squirrel
  public static String fromBech32(String strBech32) throws Exception {
    byte[] data = Bech32.decode(strBech32).data;

    data = convertBits(data, 5, 8, true);

    if (data == null) {
      throw new RuntimeException("Invalid null data");
    }
    // Remove trailing bit
    data = Arrays.copyOfRange(data, 0, data.length - 1);

    return NostrUtil.bytesToHex(data);
  }

  /**
   * Encode a Bech32 string.
   *
   * @param bech32 input container holding encoding, hrp and data
   * @return encoded Bech32 string
   * @throws Exception if inputs are invalid or encoding fails
   */
  public static String encode(final Bech32Data bech32) throws Exception {
    return encode(bech32.encoding, bech32.hrp, bech32.data);
  }

  /**
   * Encode a Bech32 string.
   *
   * @param encoding the Bech32 variant (BECH32 or BECH32M)
   * @param hrp the human-readable prefix
   * @param values 5-bit data payload
   * @return encoded Bech32 string
   * @throws Exception if inputs are invalid or encoding fails
   */
  // Modified to throw Exceptions
  public static String encode(Encoding encoding, String hrp, final byte[] values) throws Exception {
    if (hrp.isEmpty()) {
      throw new Exception("Human-readable part is too short");
    }

    hrp = hrp.toLowerCase(Locale.ROOT);
    byte[] checksum = createChecksum(encoding, hrp, values);
    byte[] combined = new byte[values.length + checksum.length];
    System.arraycopy(values, 0, combined, 0, values.length);
    System.arraycopy(checksum, 0, combined, values.length, checksum.length);
    StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
    sb.append(hrp);
    sb.append('1');
    for (byte b : combined) {
      sb.append(CHARSET.charAt(b));
    }
    return sb.toString();
  }

  /**
   * Decode a Bech32 string.
   *
   * @param str input Bech32 string
   * @return decoded container with encoding, hrp and raw 5-bit data
   * @throws Exception if input is malformed or decodes to invalid values
   */
  // Modified to throw Exceptions
  public static Bech32Data decode(final String str) throws Exception {
    boolean lower = false, upper = false;
    if (str.length() < 8) {
      throw new Exception("Input too short: " + str.length());
    }
    for (int i = 0; i < str.length(); ++i) {
      char c = str.charAt(i);
      if (c < 33 || c > 126) {
        throw new Exception(String.format("Invalid Character %c, %d", c, i));
      }
      if (c >= 'a' && c <= 'z') {
        if (upper) {
          throw new Exception(String.format("Invalid Character %c, %d", c, i));
        }
        lower = true;
      }
      if (c >= 'A' && c <= 'Z') {
        if (lower) {
          throw new Exception(String.format("Invalid Character %c, %d", c, i));
        }
        upper = true;
      }
    }
    final int pos = str.lastIndexOf('1');
    if (pos < 1) {
      throw new Exception("Missing human-readable part");
    }
    final int dataPartLength = str.length() - 1 - pos;
    if (dataPartLength < 6) {
      throw new Exception(String.format("Data part too short: %d)", dataPartLength));
    }
    byte[] values = new byte[dataPartLength];
    for (int i = 0; i < dataPartLength; ++i) {
      char c = str.charAt(i + pos + 1);
      if (CHARSET_REV[c] == -1) {
        throw new Exception(String.format("Invalid Character %c, %d", c, i + pos + 1));
      }
      values[i] = CHARSET_REV[c];
    }
    String hrp = str.substring(0, pos).toLowerCase(Locale.ROOT);
    Encoding encoding = verifyChecksum(hrp, values);
    if (encoding == null) {
      throw new Exception("InvalidChecksum");
    }
    return new Bech32Data(encoding, hrp, Arrays.copyOfRange(values, 0, values.length - 6));
  }

  /** Find the polynomial with value coefficients mod the generator as 30-bit. */
  private static int polymod(final byte[] values) {
    int c = 1;
    for (byte v_i : values) {
      int c0 = (c >>> 25) & 0xff;
      c = ((c & 0x1ffffff) << 5) ^ (v_i & 0xff);
      if ((c0 & 1) != 0) {
        c ^= 0x3b6a57b2;
      }
      if ((c0 & 2) != 0) {
        c ^= 0x26508e6d;
      }
      if ((c0 & 4) != 0) {
        c ^= 0x1ea119fa;
      }
      if ((c0 & 8) != 0) {
        c ^= 0x3d4233dd;
      }
      if ((c0 & 16) != 0) {
        c ^= 0x2a1462b3;
      }
    }
    return c;
  }

  /** Expand a HRP for use in checksum computation. */
  private static byte[] expandHrp(final String hrp) {
    int hrpLength = hrp.length();
    byte[] ret = new byte[hrpLength * 2 + 1];
    for (int i = 0; i < hrpLength; ++i) {
      int c = hrp.charAt(i) & 0x7f; // Limit to standard 7-bit ASCII
      ret[i] = (byte) ((c >>> 5) & 0x07);
      ret[i + hrpLength + 1] = (byte) (c & 0x1f);
    }
    ret[hrpLength] = 0;
    return ret;
  }

  /** Verify a checksum. */
  private static Encoding verifyChecksum(final String hrp, final byte[] values) {
    byte[] hrpExpanded = expandHrp(hrp);
    byte[] combined = new byte[hrpExpanded.length + values.length];
    System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.length);
    System.arraycopy(values, 0, combined, hrpExpanded.length, values.length);
    final int check = polymod(combined);
    return switch (check) {
      case BECH32_CONST -> Encoding.BECH32;
      case BECH32M_CONST -> Encoding.BECH32M;
      default -> null;
    };
  }

  /** Create a checksum. */
  private static byte[] createChecksum(
      final Encoding encoding, final String hrp, final byte[] values) {
    byte[] hrpExpanded = expandHrp(hrp);
    byte[] enc = new byte[hrpExpanded.length + values.length + 6];
    System.arraycopy(hrpExpanded, 0, enc, 0, hrpExpanded.length);
    System.arraycopy(values, 0, enc, hrpExpanded.length, values.length);
    int mod = polymod(enc) ^ (encoding == Encoding.BECH32 ? BECH32_CONST : BECH32M_CONST);
    byte[] ret = new byte[6];
    for (int i = 0; i < 6; ++i) {
      ret[i] = (byte) ((mod >>> (5 * (5 - i))) & 31);
    }
    return ret;
  }

  // Added by squirrel
  private static byte[] convertBits(byte[] data, int fromWidth, int toWidth, boolean pad) {
    int acc = 0;
    int bits = 0;
    List<Byte> result = new ArrayList<>();
    for (int i = 0; i < data.length; i++) {
      int value = (data[i] & 0xff) & ((1 << fromWidth) - 1);
      acc = (acc << fromWidth) | value;
      bits += fromWidth;
      while (bits >= toWidth) {
        bits -= toWidth;
        result.add((byte) ((acc >> bits) & ((1 << toWidth) - 1)));
      }
    }
    int mask = (1 << toWidth) - 1;
    if (pad) {
      if (bits > 0) {
        int partial = (acc << (toWidth - bits)) & mask;
        result.add((byte) partial);
      }
    } else if (bits == fromWidth || ((acc << (toWidth - bits)) & mask) != 0) {
      return null;
    }
    byte[] output = new byte[result.size()];
    for (int i = 0; i < output.length; i++) {
      output[i] = result.get(i);
    }
    return output;
  }
}
