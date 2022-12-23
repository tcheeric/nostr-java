package crypto.schnorr;

import crypto.util.Point;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.util.NostrUtil;

@Log
public class Schnorr {

    private static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";
    private static final String RANDOM_NUMBER_ALGORITHM_PROVIDER = "SUN";

    private static final BigInteger MAXPRIVATEKEY
            = new BigInteger("00FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364140", 16);

    /**
     * 
     * @param msg
     * @param secKey
     * @param auxRand
     * @return
     * @throws Exception 
     */
    public static byte[] sign(byte[] msg, byte[] secKey, byte[] auxRand) throws Exception {
        if (msg.length != 32) {
            throw new Exception("The message must be a 32-byte array.");
        }
        BigInteger secKey0 = NostrUtil.bigIntFromBytes(secKey);

        if (!(BigInteger.ONE.compareTo(secKey0) <= 0 && secKey0.compareTo(Point.getn().subtract(BigInteger.ONE)) <= 0)) {
            throw new Exception("The secret key must be an integer in the range 1..n-1.");
        }
        Point P = Point.mul(Point.getG(), secKey0);
        if (!P.hasEvenY()) {
            secKey0 = Point.getn().subtract(secKey0);
        }
        int len = NostrUtil.bytesFromBigInteger(secKey0).length + P.toBytes().length + msg.length;
        byte[] buf = new byte[len];
        byte[] t = NostrUtil.xor(NostrUtil.bytesFromBigInteger(secKey0), Point.taggedHash("BIP0340/aux", auxRand));
        System.arraycopy(t, 0, buf, 0, t.length);
        System.arraycopy(P.toBytes(), 0, buf, t.length, P.toBytes().length);
        System.arraycopy(msg, 0, buf, t.length + P.toBytes().length, msg.length);
        BigInteger k0 = NostrUtil.bigIntFromBytes(Point.taggedHash("BIP0340/nonce", buf)).mod(Point.getn());
        if (k0.compareTo(BigInteger.ZERO) == 0) {
            throw new Exception("Failure. This happens only with negligible probability.");
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
        BigInteger e = NostrUtil.bigIntFromBytes(Point.taggedHash("BIP0340/challenge", buf)).mod(Point.getn());
        BigInteger kes = k.add(e.multiply(secKey0)).mod(Point.getn());
        len = R.toBytes().length + NostrUtil.bytesFromBigInteger(kes).length;
        byte[] sig = new byte[len];
        System.arraycopy(R.toBytes(), 0, sig, 0, R.toBytes().length);
        System.arraycopy(NostrUtil.bytesFromBigInteger(kes), 0, sig, R.toBytes().length, NostrUtil.bytesFromBigInteger(kes).length);
        if (!verify(msg, P.toBytes(), sig)) {
            throw new Exception("The signature does not pass verification.");
        }
        return sig;
    }

    /**
     * 
     * @param msg
     * @param pubkey
     * @param sig
     * @return
     * @throws Exception 
     */
    public static boolean verify(byte[] msg, byte[] pubkey, byte[] sig) throws Exception {
        if (msg.length != 32) {
            throw new Exception("The message must be a 32-byte array.");
        }
        if (pubkey.length != 32) {
            throw new Exception("The public key must be a 32-byte array.");
        }
        if (sig.length != 64) {
            throw new Exception("The signature must be a 64-byte array.");
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
        BigInteger e = NostrUtil.bigIntFromBytes(Point.taggedHash("BIP0340/challenge", buf)).mod(Point.getn());
        Point R = Point.add(Point.mul(Point.getG(), s), Point.mul(P, Point.getn().subtract(e)));
        return R != null && R.hasEvenY() && R.getX().compareTo(r) == 0;
    }

    /**
     * Generate a random private key that can be used with Secp256k1.
     *
     * @return 
     */
    public static byte[] generatePrivateKey() {
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM, RANDOM_NUMBER_ALGORITHM_PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            log.log(Level.SEVERE, null, e);
            secureRandom = new SecureRandom();
        }

        // Generate the key, skipping as many as desired.
        byte[] privateKeyAttempt = new byte[32];
        secureRandom.nextBytes(privateKeyAttempt);
        BigInteger privateKeyCheck = new BigInteger(1, privateKeyAttempt);

        while (privateKeyCheck.compareTo(BigInteger.ZERO) == 0
                || privateKeyCheck.compareTo(MAXPRIVATEKEY) == 1) {
            secureRandom.nextBytes(privateKeyAttempt);
            privateKeyCheck = new BigInteger(1, privateKeyAttempt);
        }

        return privateKeyAttempt;
    }

    public static byte[] genPubKey(byte[] secKey) throws Exception {
        BigInteger x = NostrUtil.bigIntFromBytes(secKey);
        if (!(BigInteger.ONE.compareTo(x) <= 0 && x.compareTo(Point.getn().subtract(BigInteger.ONE)) <= 0)) {
            throw new Exception("The secret key must be an integer in the range 1..n-1.");
        }
        Point ret = Point.mul(Point.G, x);
        return Point.bytesFromPoint(ret);
    }
}
