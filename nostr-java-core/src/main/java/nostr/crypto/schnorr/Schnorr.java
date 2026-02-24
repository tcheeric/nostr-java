package nostr.crypto.schnorr;

import nostr.crypto.Point;
import nostr.util.NostrUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
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
    int len = NostrUtil.bytesFromBigInteger(secKey0).length + P.toBytes().length + msg.length;
    byte[] buf = new byte[len];
    byte[] t =
        NostrUtil.xor(
            NostrUtil.bytesFromBigInteger(secKey0), taggedHashUnchecked("BIP0340/aux", auxRand));

    if (t == null) {
      throw new RuntimeException("Unexpected error. Null array");
    }

    System.arraycopy(t, 0, buf, 0, t.length);
    System.arraycopy(P.toBytes(), 0, buf, t.length, P.toBytes().length);
    System.arraycopy(msg, 0, buf, t.length + P.toBytes().length, msg.length);
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
    len = R.toBytes().length + P.toBytes().length + msg.length;
    buf = new byte[len];
    System.arraycopy(R.toBytes(), 0, buf, 0, R.toBytes().length);
    System.arraycopy(P.toBytes(), 0, buf, R.toBytes().length, P.toBytes().length);
    System.arraycopy(msg, 0, buf, R.toBytes().length + P.toBytes().length, msg.length);
    BigInteger e =
        NostrUtil.bigIntFromBytes(taggedHashUnchecked("BIP0340/challenge", buf)).mod(Point.getn());
    BigInteger kes = k.add(e.multiply(secKey0)).mod(Point.getn());
    len = R.toBytes().length + NostrUtil.bytesFromBigInteger(kes).length;
    byte[] sig = new byte[len];
    System.arraycopy(R.toBytes(), 0, sig, 0, R.toBytes().length);
    System.arraycopy(
        NostrUtil.bytesFromBigInteger(kes),
        0,
        sig,
        R.toBytes().length,
        NostrUtil.bytesFromBigInteger(kes).length);
    if (!verify(msg, P.toBytes(), sig)) {
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

    Point P = Point.liftX(pubkey);
    if (P == null) {
      return false;
    }
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
    Point R = Point.add(Point.mul(Point.getG(), s), Point.mul(P, Point.getn().subtract(e)));
    return R != null && R.hasEvenY() && R.getX().compareTo(r) == 0;
  }

  /**
   * Generate a random private key suitable for secp256k1.
   *
   * @return a 32-byte private key
   */
  public static byte[] generatePrivateKey() {
    try {
      Security.addProvider(new BouncyCastleProvider());
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "BC");
      kpg.initialize(new ECGenParameterSpec("secp256k1"), SecureRandom.getInstanceStrong());
      KeyPair processorKeyPair = kpg.genKeyPair();

      return NostrUtil.bytesFromBigInteger(((ECPrivateKey) processorKeyPair.getPrivate()).getS());

    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException e) {
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
