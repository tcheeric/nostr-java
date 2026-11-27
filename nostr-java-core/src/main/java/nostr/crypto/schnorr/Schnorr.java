package nostr.crypto.schnorr;

import nostr.crypto.Point;
import nostr.util.NostrUtil;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.WNafUtil;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utility methods for BIP-340 Schnorr signatures over secp256k1.
 *
 * <p>Implements signing, verification, and simple key derivation helpers used throughout the
 * project. All methods operate on 32-byte inputs as mandated by the specification.
 */
public class Schnorr {

  /**
   * Create a Schnorr signature for a 32-byte message.
   *
   * @param msg 32-byte message hash to sign
   * @param secKey 32-byte secret key
   * @param auxRand auxiliary 32 random bytes used for nonce derivation
   * @return the 64-byte signature (R || s)
   * @throws SchnorrException if inputs are invalid or signing fails
   */
  public static byte[] sign(byte[] msg, byte[] secKey, byte[] auxRand) throws SchnorrException {
    if (msg.length != 32) {
      throw new SchnorrException("The message must be a 32-byte array.");
    }
    BigInteger secKey0 = NostrUtil.bigIntFromBytes(secKey);

    if (!(BigInteger.ONE.compareTo(secKey0) <= 0
        && secKey0.compareTo(Point.getn().subtract(BigInteger.ONE)) <= 0)) {
      throw new SchnorrException("The secret key must be an integer in the range 1..n-1.");
    }
    Point P = Point.mul(Point.getG(), secKey0);
    if (!P.hasEvenY()) {
      secKey0 = Point.getn().subtract(secKey0);
    }
    byte[] pBytes = P.toBytes();
    byte[] t = NostrUtil.xor(NostrUtil.bytesFromBigInteger(secKey0), taggedHashUnchecked("BIP0340/aux", auxRand));

    if (t == null) {
      throw new RuntimeException("Unexpected error. Null array");
    }

    byte[] buf = new byte[t.length + pBytes.length + msg.length];
    System.arraycopy(t, 0, buf, 0, t.length);
    System.arraycopy(pBytes, 0, buf, t.length, pBytes.length);
    System.arraycopy(msg, 0, buf, t.length + pBytes.length, msg.length);
    BigInteger k0 =
        NostrUtil.bigIntFromBytes(taggedHashUnchecked("BIP0340/nonce", buf)).mod(Point.getn());
    if (k0.compareTo(BigInteger.ZERO) == 0) {
      throw new SchnorrException("Failure. This happens only with negligible probability.");
    }
    Point R = Point.mul(Point.getG(), k0);
    BigInteger k;
    if (!R.hasEvenY()) {
      k = Point.getn().subtract(k0);
    } else {
      k = k0;
    }
    byte[] rBytes = R.toBytes();
    buf = new byte[rBytes.length + pBytes.length + msg.length];
    System.arraycopy(rBytes, 0, buf, 0, rBytes.length);
    System.arraycopy(pBytes, 0, buf, rBytes.length, pBytes.length);
    System.arraycopy(msg, 0, buf, rBytes.length + pBytes.length, msg.length);
    BigInteger e =
        NostrUtil.bigIntFromBytes(taggedHashUnchecked("BIP0340/challenge", buf)).mod(Point.getn());
    BigInteger kes = k.add(e.multiply(secKey0)).mod(Point.getn());
    byte[] kesBytes = NostrUtil.bytesFromBigInteger(kes);
    byte[] sig = new byte[rBytes.length + kesBytes.length];
    System.arraycopy(rBytes, 0, sig, 0, rBytes.length);
    System.arraycopy(kesBytes, 0, sig, rBytes.length, kesBytes.length);
    // The public key point is already known, so the self-check skips lifting it again.
    ECPoint pEven = liftCached(pBytes);
    if (pEven == null || !verify(msg, pBytes, pEven, sig)) {
      throw new SchnorrException("The signature does not pass verification.");
    }
    return sig;
  }

  /**
   * Verify a Schnorr signature for a 32-byte message.
   *
   * @param msg 32-byte message hash to verify
   * @param pubkey 32-byte x-only public key
   * @param sig 64-byte signature (R || s)
   * @return true if the signature is valid; false otherwise
   * @throws SchnorrException if inputs are invalid
   */
  public static boolean verify(byte[] msg, byte[] pubkey, byte[] sig) throws SchnorrException {

    if (msg.length != 32) {
      throw new SchnorrException("The message must be a 32-byte array.");
    }

    if (pubkey.length != 32) {
      throw new SchnorrException("The public key must be a 32-byte array.");
    }
    if (sig.length != 64) {
      throw new SchnorrException("The signature must be a 64-byte array.");
    }

    ECPoint P = liftCached(pubkey);
    if (P == null) {
      return false;
    }
    return verify(msg, pubkey, P, sig);
  }

  private static final X9ECParameters BC = CustomNamedCurves.getByName("secp256k1");
  private static final ECPoint BC_G = BC.getG();
  private static final int LIFTED_CAPACITY = 1024;
  private static final ConcurrentHashMap<ByteKey, ECPoint> LIFTED = new ConcurrentHashMap<>();

  private record ByteKey(byte[] b) {
    @Override
    public boolean equals(Object o) {
      return o instanceof ByteKey k && Arrays.equals(b, k.b);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(b);
    }
  }

  /**
   * The even-y point for an x-only key, or {@code null} if x is not on the curve.
   *
   * <p>Verifiers see few distinct keys, and BouncyCastle keeps its multiplication tables on the
   * point object, so a cached point (configured with the wider basepoint wNAF window) keeps its
   * tables across calls. Bounded; cleared wholesale when full, which is cheap and never wrong.
   */
  static ECPoint liftCached(byte[] x) {
    ByteKey key = new ByteKey(x);
    ECPoint cached = LIFTED.get(key);
    if (cached != null) {
      return cached;
    }
    if (new BigInteger(1, x).compareTo(Point.getp()) >= 0) {
      return null;
    }
    byte[] enc = new byte[33];
    enc[0] = 0x02;
    System.arraycopy(x, 0, enc, 1, 32);
    ECPoint point;
    try {
      point = BC.getCurve().decodePoint(enc);
    } catch (IllegalArgumentException notOnCurve) {
      return null;
    }
    WNafUtil.configureBasepoint(point);
    if (LIFTED.size() >= LIFTED_CAPACITY) {
      LIFTED.clear();
    }
    LIFTED.put(new ByteKey(x.clone()), point);
    return point;
  }

  private static boolean verify(byte[] msg, byte[] pubkey, ECPoint P, byte[] sig)
      throws SchnorrException {
    BigInteger r = NostrUtil.bigIntFromBytes(Arrays.copyOfRange(sig, 0, 32));
    BigInteger s = NostrUtil.bigIntFromBytes(Arrays.copyOfRange(sig, 32, 64));
    if (r.compareTo(Point.getp()) >= 0 || s.compareTo(Point.getn()) >= 0) {
      return false;
    }
    int len = 32 + pubkey.length + msg.length;
    byte[] buf = new byte[len];
    System.arraycopy(sig, 0, buf, 0, 32);
    System.arraycopy(pubkey, 0, buf, 32, pubkey.length);
    System.arraycopy(msg, 0, buf, 32 + pubkey.length, msg.length);
    BigInteger e =
        NostrUtil.bigIntFromBytes(taggedHashUnchecked("BIP0340/challenge", buf)).mod(Point.getn());
    ECPoint R = ECAlgorithms.sumOfTwoMultiplies(BC_G, s, P, Point.getn().subtract(e)).normalize();
    return !R.isInfinity()
        && !R.getAffineYCoord().testBitZero()
        && R.getAffineXCoord().toBigInteger().equals(r);
  }

  /**
   * Generate a random private key suitable for secp256k1.
   *
   * <p>Uses BouncyCastle's lightweight API rather than a JCE provider lookup. The
   * old implementation called {@code Security.addProvider(new BouncyCastleProvider())}
   * here and then asked for {@code KeyPairGenerator.getInstance("ECDSA", "BC")} —
   * which made every other BouncyCastle-dependent code path in the library work only
   * once a key had been generated first. NIP-44 was the visible casualty; see
   * {@link nostr.crypto.nip44.EncryptedPayloads}. Registering a provider is a global,
   * process-wide side effect and is the caller's business, not a key generator's.
   *
   * @return a 32-byte private key
   */
  public static byte[] generatePrivateKey() {
    try {
      ECNamedCurveParameterSpec curve = ECNamedCurveTable.getParameterSpec("secp256k1");
      ECDomainParameters domain =
          new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
      ECKeyPairGenerator generator = new ECKeyPairGenerator();
      generator.init(new ECKeyGenerationParameters(domain, SecureRandom.getInstanceStrong()));
      ECPrivateKeyParameters privateKey =
          (ECPrivateKeyParameters) generator.generateKeyPair().getPrivate();

      return NostrUtil.bytesFromBigInteger(privateKey.getD());

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Derive the x-only public key bytes for a given private key.
   *
   * @param secKey 32-byte secret key
   * @return the 32-byte x-only public key
   * @throws SchnorrException if the private key is out of range
   */
  public static byte[] genPubKey(byte[] secKey) throws SchnorrException {
    BigInteger x = NostrUtil.bigIntFromBytes(secKey);
    if (!(BigInteger.ONE.compareTo(x) <= 0
        && x.compareTo(Point.getn().subtract(BigInteger.ONE)) <= 0)) {
      throw new SchnorrException("The secret key must be an integer in the range 1..n-1.");
    }
    Point ret = Point.mul(Point.G, x);
    return Point.bytesFromPoint(ret);
  }

  private static byte[] taggedHashUnchecked(String tag, byte[] msg) {
    try {
      return Point.taggedHash(tag, msg);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
