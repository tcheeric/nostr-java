package nostr.crypto.schnorr;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Holds {@link Schnorr} to a plain-BigInteger transcription of the BIP-340 reference code: the
 * same signatures for random keys, and the same verdict on valid, corrupted and out-of-range
 * inputs. The optimised verify caches lifted keys, so the corrupted cases are run after the key
 * is cached, and keys whose x is off the curve must never be cached as valid.
 */
class SchnorrReferenceTest {

  private static final BigInteger P =
      new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
  private static final BigInteger N =
      new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
  private static final BigInteger[] G = {
    new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
    new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
  };

  private final Random rnd = new Random(7);

  // ------------------------------------------------------------- BIP-340 reference (affine)

  private static BigInteger[] add(BigInteger[] a, BigInteger[] b) {
    if (a == null) return b;
    if (b == null) return a;
    if (a[0].equals(b[0]) && !a[1].equals(b[1])) return null;
    BigInteger lam =
        a[0].equals(b[0])
            ? BigInteger.valueOf(3).multiply(a[0].pow(2)).multiply(a[1].shiftLeft(1).modPow(P.subtract(BigInteger.TWO), P)).mod(P)
            : b[1].subtract(a[1]).multiply(b[0].subtract(a[0]).modPow(P.subtract(BigInteger.TWO), P)).mod(P);
    BigInteger x = lam.pow(2).subtract(a[0]).subtract(b[0]).mod(P);
    return new BigInteger[] {x, lam.multiply(a[0].subtract(x)).subtract(a[1]).mod(P)};
  }

  private static BigInteger[] mul(BigInteger[] p, BigInteger k) {
    BigInteger[] r = null;
    for (int i = k.bitLength() - 1; i >= 0; i--) {
      r = add(r, r);
      if (k.testBit(i)) r = add(r, p);
    }
    return r;
  }

  private static byte[] b32(BigInteger v) {
    byte[] raw = v.toByteArray();
    byte[] out = new byte[32];
    int n = Math.min(raw.length, 32);
    System.arraycopy(raw, raw.length - n, out, 32 - n, n);
    return out;
  }

  private static byte[] tagged(String tag, byte[]... parts) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] t = md.digest(tag.getBytes(StandardCharsets.UTF_8));
    md.update(t);
    md.update(t);
    for (byte[] p : parts) md.update(p);
    return md.digest();
  }

  private static BigInteger[] liftX(byte[] xb) {
    BigInteger x = new BigInteger(1, xb);
    if (x.compareTo(P) >= 0) return null;
    BigInteger c = x.modPow(BigInteger.valueOf(3), P).add(BigInteger.valueOf(7)).mod(P);
    BigInteger y = c.modPow(P.add(BigInteger.ONE).shiftRight(2), P);
    if (!y.modPow(BigInteger.TWO, P).equals(c)) return null;
    return new BigInteger[] {x, y.testBit(0) ? P.subtract(y) : y};
  }

  static boolean refVerify(byte[] msg, byte[] pk, byte[] sig) throws Exception {
    BigInteger[] p = liftX(pk);
    if (p == null) return false;
    BigInteger r = new BigInteger(1, Arrays.copyOfRange(sig, 0, 32));
    BigInteger s = new BigInteger(1, Arrays.copyOfRange(sig, 32, 64));
    if (r.compareTo(P) >= 0 || s.compareTo(N) >= 0) return false;
    BigInteger e = new BigInteger(1, tagged("BIP0340/challenge", Arrays.copyOfRange(sig, 0, 32), pk, msg)).mod(N);
    BigInteger[] R = add(mul(G, s), mul(p, N.subtract(e)));
    return R != null && !R[1].testBit(0) && R[0].equals(r);
  }

  static byte[] refSign(byte[] msg, byte[] sk, byte[] aux) throws Exception {
    BigInteger d0 = new BigInteger(1, sk);
    BigInteger[] p = mul(G, d0);
    BigInteger d = p[1].testBit(0) ? N.subtract(d0) : d0;
    byte[] t = b32(d);
    byte[] a = tagged("BIP0340/aux", aux);
    for (int i = 0; i < 32; i++) t[i] ^= a[i];
    BigInteger k0 = new BigInteger(1, tagged("BIP0340/nonce", t, b32(p[0]), msg)).mod(N);
    BigInteger[] R = mul(G, k0);
    BigInteger k = R[1].testBit(0) ? N.subtract(k0) : k0;
    BigInteger e = new BigInteger(1, tagged("BIP0340/challenge", b32(R[0]), b32(p[0]), msg)).mod(N);
    byte[] sig = new byte[64];
    System.arraycopy(b32(R[0]), 0, sig, 0, 32);
    System.arraycopy(b32(k.add(e.multiply(d)).mod(N)), 0, sig, 32, 32);
    return sig;
  }

  // ------------------------------------------------------------------------------- tests

  private byte[] bytes(int n) {
    byte[] b = new byte[n];
    rnd.nextBytes(b);
    return b;
  }

  private static boolean ours(byte[] m, byte[] pk, byte[] sig) {
    try {
      return Schnorr.verify(m, pk, sig);
    } catch (SchnorrException e) {
      return false;
    }
  }

  private static void agree(byte[] m, byte[] pk, byte[] sig) throws Exception {
    assertEquals(refVerify(m, pk, sig), ours(m, pk, sig), "verdict for pk " + new BigInteger(1, pk).toString(16));
  }

  @Test
  void signaturesAndKeysMatchTheReference() throws Exception {
    for (int i = 0; i < 40; i++) {
      byte[] sk = bytes(32);
      byte[] msg = bytes(32);
      byte[] aux = i % 4 == 0 ? new byte[32] : bytes(32);
      assertArrayEquals(b32(mul(G, new BigInteger(1, sk))[0]), Schnorr.genPubKey(sk));
      assertArrayEquals(refSign(msg, sk, aux), Schnorr.sign(msg, sk, aux));
    }
  }

  @Test
  void verdictsMatchTheReferenceOnceKeysAreCached() throws Exception {
    for (int i = 0; i < 40; i++) {
      byte[] sk = bytes(32);
      byte[] msg = bytes(32);
      byte[] pk = Schnorr.genPubKey(sk);
      byte[] sig = Schnorr.sign(msg, sk, bytes(32));
      agree(msg, pk, sig); // caches pk
      byte[] flipped = sig.clone();
      flipped[rnd.nextInt(64)] ^= (byte) (1 << rnd.nextInt(8));
      agree(msg, pk, flipped);
      byte[] otherMsg = msg.clone();
      otherMsg[0] ^= 1;
      agree(otherMsg, pk, sig);
      byte[] otherPk = pk.clone();
      otherPk[rnd.nextInt(32)] ^= 1; // usually off the curve or another key
      agree(msg, otherPk, sig);
      agree(msg, otherPk, sig); // second time: must not have been cached as valid
    }
  }

  @Test
  void verdictsMatchTheReferenceOnOutOfRangeValues() throws Exception {
    byte[] sk = bytes(32);
    byte[] msg = bytes(32);
    byte[] pk = Schnorr.genPubKey(sk);
    byte[] sig = Schnorr.sign(msg, sk, bytes(32));
    byte[] rIsP = sig.clone();
    System.arraycopy(b32(P), 0, rIsP, 0, 32);
    agree(msg, pk, rIsP);
    byte[] sIsN = sig.clone();
    System.arraycopy(b32(N), 0, sIsN, 32, 32);
    agree(msg, pk, sIsN);
    agree(msg, b32(P), sig);
    agree(msg, b32(P.add(BigInteger.ONE)), sig);
    agree(msg, new byte[32], sig);
    byte[] ff = new byte[32];
    Arrays.fill(ff, (byte) 0xff);
    agree(msg, ff, sig);
    agree(msg, pk, new byte[64]);
  }
}
