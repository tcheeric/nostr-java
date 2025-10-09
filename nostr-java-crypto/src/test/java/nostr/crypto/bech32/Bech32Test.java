package nostr.crypto.bech32;

import static org.junit.jupiter.api.Assertions.*;

import nostr.crypto.bech32.Bech32.Bech32Data;
import nostr.crypto.bech32.Bech32.Encoding;
import org.junit.jupiter.api.Test;

/** Tests for Bech32 encode/decode and NIP-19 helpers. */
public class Bech32Test {

  private static final String HEX64 = "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d";
  private static final String NPUB_FOR_HEX64 = "npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6";

  @Test
  void toFromBech32RoundtripNpub() throws Exception {
    String npub = Bech32.toBech32(Bech32Prefix.NPUB, HEX64);
    assertTrue(npub.startsWith("npub"));
    String hex = Bech32.fromBech32(npub);
    assertEquals(HEX64, hex);
  }

  @Test
  void knownVectorNpub() throws Exception {
    // As documented in Bech32 Javadoc
    String npub = Bech32.toBech32(Bech32Prefix.NPUB, HEX64);
    assertEquals(NPUB_FOR_HEX64, npub);
    assertEquals(HEX64, Bech32.fromBech32(NPUB_FOR_HEX64));
  }

  @Test
  void lowLevelEncodeDecode() throws Exception {
    byte[] fiveBit = new byte[] {0,1,2,3,4,5,6,7,8,9};
    String s = Bech32.encode(Encoding.BECH32, "hrp", fiveBit);
    Bech32Data d = Bech32.decode(s);
    assertEquals("hrp", d.hrp);
    assertEquals(Encoding.BECH32, d.encoding);
    assertArrayEquals(fiveBit, d.data);
  }

  @Test
  void decodeRejectsInvalidCharsAndChecksum() {
    assertThrows(Exception.class, () -> Bech32.decode("tooshort"));
    assertThrows(Exception.class, () -> Bech32.decode("HRP1INV@LID"));
    // wrong checksum
    assertThrows(Exception.class, () -> Bech32.decode("hrp1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"));
  }

  @Test
  void bech32mEncodeDecode() throws Exception {
    byte[] fiveBit = new byte[] {1,1,2,3,5,8,13};
    String s = Bech32.encode(Encoding.BECH32M, "nprof", fiveBit);
    Bech32Data d = Bech32.decode(s);
    assertEquals(Encoding.BECH32M, d.encoding);
    assertEquals("nprof", d.hrp);
    assertArrayEquals(fiveBit, d.data);
  }

  @Test
  void toBech32ForOtherPrefixes() {
    String nsec = Bech32.toBech32(Bech32Prefix.NSEC, HEX64);
    assertTrue(nsec.startsWith("nsec"));
    String note = Bech32.toBech32(Bech32Prefix.NOTE, HEX64);
    assertTrue(note.startsWith("note"));
  }

  @Test
  void fromBech32RejectsMalformed() {
    // Missing separator
    assertThrows(Exception.class, () -> Bech32.fromBech32("npub"));
    // Invalid character
    assertThrows(Exception.class, () -> Bech32.fromBech32("npub1inv@lid"));
    // Short data part
    assertThrows(Exception.class, () -> Bech32.fromBech32("npub1qqqq"));
  }
}
