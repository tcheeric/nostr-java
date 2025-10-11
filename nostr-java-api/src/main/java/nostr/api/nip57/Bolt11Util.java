package nostr.api.nip57;

import java.util.Locale;

/** Utility to parse msats from a BOLT11 invoice HRP. */
public final class Bolt11Util {

  private Bolt11Util() {}

  /**
   * Parse millisatoshi amount from a BOLT11 invoice.
   *
   * Supports amounts encoded in the HRP using multipliers 'm', 'u', 'n', 'p'. If the invoice has
   * no amount, returns -1 to indicate unknown/any amount.
   *
   * @param bolt11 bech32 invoice string
   * @return amount in millisatoshis, or -1 if no amount present
   * @throws IllegalArgumentException if the HRP is invalid or the amount cannot be parsed
   */
  public static long parseMsat(String bolt11) {
    if (bolt11 == null || bolt11.isBlank()) {
      throw new IllegalArgumentException("bolt11 invoice is required");
    }
    String lower = bolt11.toLowerCase(Locale.ROOT);
    int sep = lower.indexOf('1');
    if (!lower.startsWith("ln") || sep < 0) {
      throw new IllegalArgumentException("Invalid BOLT11 invoice: missing HRP separator");
    }
    String hrp = lower.substring(2, sep); // drop leading "ln"
    // Expect network code (bc, tb, bcrt, etc.), then amount digits with optional unit
    int idx = 0;
    while (idx < hrp.length() && Character.isAlphabetic(hrp.charAt(idx))) idx++;
    String amountPart = idx < hrp.length() ? hrp.substring(idx) : "";
    if (amountPart.isEmpty()) {
      return -1; // any amount invoice
    }
    // Split numeric and optional unit suffix
    int i = 0;
    while (i < amountPart.length() && Character.isDigit(amountPart.charAt(i))) i++;
    if (i == 0) {
      throw new IllegalArgumentException("Invalid BOLT11 amount");
    }
    long value = Long.parseLong(amountPart.substring(0, i));
    int exponent = 11; // convert BTC to msat => * 10^11
    if (i < amountPart.length()) {
      char unit = amountPart.charAt(i);
      exponent += switch (unit) {
        case 'm' -> -3; // milliBTC
        case 'u' -> -6; // microBTC
        case 'n' -> -9; // nanoBTC
        case 'p' -> -12; // picoBTC
        default -> throw new IllegalArgumentException("Unsupported BOLT11 unit: " + unit);
      };
    }
    // value * 10^exponent can overflow; restrict to safe subset used in tests
    java.math.BigInteger msat = java.math.BigInteger.valueOf(value);
    if (exponent >= 0) {
      msat = msat.multiply(java.math.BigInteger.TEN.pow(exponent));
    } else {
      msat = msat.divide(java.math.BigInteger.TEN.pow(-exponent));
    }
    if (msat.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
      throw new IllegalArgumentException("BOLT11 amount exceeds supported range");
    }
    return msat.longValue();
  }
}
