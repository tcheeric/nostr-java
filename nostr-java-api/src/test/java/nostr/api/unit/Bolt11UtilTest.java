package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nostr.api.nip57.Bolt11Util;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Bolt11Util amount parsing.
 */
public class Bolt11UtilTest {

  @Test
  // Parses nanoBTC amount (n) into msat. Example: 50n BTC → 5000 msat.
  void parseNanoBtcToMsat() {
    // 50n BTC = 50 * 10^-9 BTC → 50 * 10^2 sat → 5000 msat
    long msat = Bolt11Util.parseMsat("lnbc50n1pxyz");
    assertEquals(5_000L, msat);
  }

  @Test
  // Parses picoBTC amount (p) into msat. Example: 2000p BTC → 200 msat.
  void parsePicoBtcToMsat() {
    // 2000p BTC = 2000 * 10^-12 BTC → 0.2 sat → 200 msat
    long msat = Bolt11Util.parseMsat("lnbc2000p1pabc");
    assertEquals(200L, msat);
  }

  @Test
  // Invoice without amount returns -1 to indicate any-amount invoice.
  void parseNoAmountInvoice() {
    long msat = Bolt11Util.parseMsat("lnbc1pnoamount");
    assertEquals(-1L, msat);
  }

  @Test
  // Invalid HRP throws IllegalArgumentException.
  void invalidInvoiceThrows() {
    assertThrows(IllegalArgumentException.class, () -> Bolt11Util.parseMsat("notbolt11"));
  }

  @Test
  // Parses milliBTC (m) unit into msat. Example: 2m BTC → 200,000,000 msat.
  void parseMilliBtcToMsat() {
    long msat = Bolt11Util.parseMsat("lnbc2m1ptest");
    assertEquals(200_000_000L, msat);
  }

  @Test
  // Parses microBTC (u) unit into msat. Example: 25u BTC → 2,500,000 msat.
  void parseMicroBtcToMsat() {
    long msat = Bolt11Util.parseMsat("lntb25u1ptest");
    assertEquals(2_500_000L, msat);
  }

  @Test
  // Parses BTC with no unit. Example: 1 BTC → 100,000,000,000 msat.
  void parseWholeBtcNoUnit() {
    long msat = Bolt11Util.parseMsat("lnbc11some");
    assertEquals(100_000_000_000L, msat);
  }

  @Test
  // Accepts uppercase invoice strings by normalizing to lowercase.
  void parseUppercaseInvoice() {
    long msat = Bolt11Util.parseMsat("LNBC50N1PUPPER");
    assertEquals(5_000L, msat);
  }

  @Test
  // Supports testnet network code (lntb...).
  void parseTestnetNano() {
    long msat = Bolt11Util.parseMsat("lntb50n1pxyz");
    assertEquals(5_000L, msat);
  }

  @Test
  // Supports regtest network code (lnbcrt...).
  void parseRegtestNano() {
    long msat = Bolt11Util.parseMsat("lnbcrt50n1pxyz");
    assertEquals(5_000L, msat);
  }

  @Test
  // Excessively large amounts should throw due to overflow protection.
  void parseTooLargeThrows() {
    // This crafts a huge value: 9999999999999999999m BTC -> will exceed Long.MAX_VALUE in msat
    String huge = "lnbc9999999999999999999m1pbig";
    assertThrows(IllegalArgumentException.class, () -> Bolt11Util.parseMsat(huge));
  }
}
