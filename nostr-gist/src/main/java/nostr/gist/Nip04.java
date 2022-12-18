package nostr.gist;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author squirrel
 */
public class Nip04 {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static void main(String[] args) throws NostrException, Exception {
        try {
            var senderPrivateKey = hexToBytes("16998b94e199168e14f67d5cf669202d813d57c0a5500296a6f909ce1fc4fdbb");
            var rcptPublicKey = hexToBytes("01739eae78ef308acb9e7a8a85f7d03484e0d338a7fae1ef2a8fa18e9b5915c5");

            String encryptedMessage = encryptMessage(senderPrivateKey, rcptPublicKey, "Hello Nostr!");

            System.out.println("senderPrivateKey: " + bytesToHex(senderPrivateKey));
            System.out.println("rcptPublicKey: " + bytesToHex(rcptPublicKey));
            System.out.println("Message: " + "Hello Nostr!");
            System.out.println("Encrypted message: " + encryptedMessage);

        } catch (NoSuchAlgorithmException | IllegalArgumentException ex) {
            throw new NostrException(ex);
        }
    }

    private static String encryptMessage(byte[] senderPrivateKey, byte[] rcptPublicKey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NostrException {

        final var Base64Encoder = Base64.getEncoder();
        final var msg = message.getBytes(StandardCharsets.UTF_8);

        // Calculate the shared secret
        var sharedPoint = getSharedSecret(bytesToHex(senderPrivateKey), "02" + bytesToHex(rcptPublicKey));
        var sharedX = Arrays.copyOfRange(sharedPoint, 1, 33);

        SecretKeySpec sharedSecretKey = new SecretKeySpec(sharedX, "AES");

        var iv = createRandomByteArray(16);
        var ivParamSpec = new IvParameterSpec(iv);

        // Encrypt the message
        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sharedSecretKey, ivParamSpec);
        var encryptedMessage1 = Base64Encoder.encode(cipher.update(msg));
        var encryptedMessage2 = Base64Encoder.encode(cipher.doFinal());

        // Concatenate the two arrays
        var result = Arrays.copyOf(encryptedMessage1, encryptedMessage1.length + encryptedMessage2.length);
        System.arraycopy(encryptedMessage2, 0, result, encryptedMessage1.length, encryptedMessage2.length);

        var iv64 = Base64Encoder.encode(ivParamSpec.getIV());

        // Return the encrypted DM event content
        return new String(result) + "?iv=" + new String(iv64);
    }

    public static String bytesToHex(byte[] b) {
        char[] hexChars = new char[b.length * 2];
        for (int j = 0; j < b.length; j++) {
            int v = b[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    public static byte[] createRandomByteArray(int len) {
        byte[] b = new byte[len];
        new Random().nextBytes(b);
        return b;
    }

    private static byte[] getSharedSecret(String privateKeyHex, String publicKeyHex) throws NostrException {

        Point pubKeyPt = Point.fromHex(publicKeyHex);

        BigInteger privKey = new BigInteger(hexToBytes(privateKeyHex));

        return Point.mul(pubKeyPt, privKey).toBytes();
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] buf = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            buf[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return buf;
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

    private static BigInteger bigIntFromBytes(byte[] b) {
        return new BigInteger(1, b);
    }

    private static byte[] sha256(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(b);
    }

    public static class Point {

        final static private BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
        final static private BigInteger n = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);

        final static public Point G = new Point(
                new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
                new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
        );

        private static final BigInteger BI_TWO = BigInteger.valueOf(2);
        private final Pair<BigInteger, BigInteger> pair;

        public Point(BigInteger x, BigInteger y) {
            pair = Pair.of(x, y);
        }

        public Point(byte[] b0, byte[] b1) {
            pair = Pair.of(new BigInteger(1, b0), new BigInteger(1, b1));
        }

        public static BigInteger getp() {
            return p;
        }

        public static BigInteger getn() {
            return n;
        }

        @SuppressWarnings("SameReturnValue")
        public static Point getG() {
            return G;
        }

        public BigInteger getX() {
            return pair.getLeft();
        }

        public BigInteger getY() {
            return pair.getRight();
        }

        public static BigInteger getX(Point P) {
            assert !P.isInfinite();
            return P.getX();
        }

        public static BigInteger getY(Point P) {
            assert !P.isInfinite();
            return P.getY();
        }

        public Pair<BigInteger, BigInteger> getPair() {
            return pair;
        }

        public boolean isInfinite() {
            return pair == null || pair.getLeft() == null || pair.getRight() == null;
        }

        public static boolean isInfinite(Point P) {
            return P.isInfinite();
        }

        public Point add(Point P) {
            return add(this, P);
        }

        public static Point add(Point P1, Point P2) {

            if ((P1 != null && P2 != null && P1.isInfinite() && P2.isInfinite())) {
                return infinityPoint();
            }
            if (P1 == null || P1.isInfinite()) {
                return P2;
            }
            if (P2 == null || P2.isInfinite()) {
                return P1;
            }
            if (P1.getX().equals(P2.getX()) && !P1.getY().equals(P2.getY())) {
                return infinityPoint();
            }

            BigInteger lam;
            if (P1.equals(P2)) {
                BigInteger base = P2.getY().multiply(BI_TWO);
                lam = (BigInteger.valueOf(3L).multiply(P1.getX()).multiply(P1.getX()).multiply(base.modPow(p.subtract(BI_TWO), p))).mod(p);
            } else {
                BigInteger base = P2.getX().subtract(P1.getX());
                lam = ((P2.getY().subtract(P1.getY())).multiply(base.modPow(p.subtract(BI_TWO), p))).mod(p);
            }

            BigInteger x3 = (lam.multiply(lam).subtract(P1.getX()).subtract(P2.getX())).mod(p);
            return new Point(x3, lam.multiply(P1.getX().subtract(x3)).subtract(P1.getY()).mod(p));
        }

        public static Point substr(Point p, Point other) {
            return add(p, negate(other));
        }

        public static Point negate(Point p) {
            return new Point(p.getX(), mod(p.getY().negate()));
        }

        public static BigInteger mod(BigInteger a, BigInteger b) {
            return a.mod(b);
        }

        public static BigInteger mod(BigInteger a) {
            return mod(a, getp());
        }

        public static Point mul(Point P, BigInteger n) {

            Point R = null;

            for (int i = 0; i < 256; i++) {
                if (n.shiftRight(i).and(BigInteger.ONE).compareTo(BigInteger.ZERO) > 0) {
                    R = add(R, P);
                }
                P = add(P, P);
            }

            return R;
        }

        public boolean hasEvenY() {
            return hasEvenY(this);
        }

        public static boolean hasEvenY(Point P) {
            return P.getY().mod(BI_TWO).compareTo(BigInteger.ZERO) == 0;
        }

        public static boolean isSquare(BigInteger x) {
            return x.modPow(p.subtract(BigInteger.ONE).mod(BI_TWO), p).longValue() == 1L;
        }

        public boolean hasSquareY() {
            return hasSquareY(this);
        }

        public static boolean hasSquareY(Point P) {
            assert !isInfinite(P);
            return isSquare(P.getY());
        }

        public static byte[] taggedHash(String tag, byte[] msg) throws NoSuchAlgorithmException {

            byte[] tagHash = sha256(tag.getBytes());
            int len = (tagHash.length * 2) + msg.length;
            byte[] buf = new byte[len];
            System.arraycopy(tagHash, 0, buf, 0, tagHash.length);
            System.arraycopy(tagHash, 0, buf, tagHash.length, tagHash.length);
            System.arraycopy(msg, 0, buf, tagHash.length * 2, msg.length);

            return sha256(buf);
        }

        public byte[] toBytes() {
            return bytesFromPoint(this);
        }

        public static byte[] bytesFromPoint(Point P) {
            return bytesFromBigInteger(P.getX());
        }

        private static Point fromCompressedHex(byte[] pubKey) throws NostrException {
            final BigInteger x = new BigInteger(Arrays.copyOfRange(pubKey, 1, pubKey.length));

            if (x.compareTo(BigInteger.ZERO) > 0 && x.compareTo(getp()) < 0) {
                var y2 = weierstrass(x);
                var y = sqrtMod(y2);
                final var yand1 = y.and(BigInteger.ONE).toByteArray();
                final var isYOdd = new BigInteger(yand1).equals(BigInteger.ONE);
                final var isFirstByteOdd = new BigInteger(pubKey).testBit(0);
                y = (isFirstByteOdd != isYOdd) ? mod(y.negate()) : y;
                return new Point(x, y);
            }
            throw new NostrException("Point is not on curve");
        }

        public static Point fromHex(String pubKey) throws NostrException {
            return fromCompressedHex(hexToBytes(pubKey));
        }

        // previously 'pointFromBytes()'
        public static Point liftX(byte[] b) {

            BigInteger x = bigIntFromBytes(b);
            if (x.compareTo(p) >= 0) {
                return null;
            }
            BigInteger y_sq = x.modPow(BigInteger.valueOf(3L), p).add(BigInteger.valueOf(7L)).mod(p);
            BigInteger y = sqrtMod(y_sq);

            if (y.modPow(BI_TWO, p).compareTo(y_sq) != 0) {
                return null;
            } else {
                return new Point(x, y.and(BigInteger.ONE).compareTo(BigInteger.ZERO) == 0 ? y : p.subtract(y));
            }
        }

        public static Point infinityPoint() {
            return new Point(null, (BigInteger) null);
        }

        public boolean equals(Point P) {
            return getPair().equals(P.getPair());
        }

        private static BigInteger weierstrass(final BigInteger x) {
            final var y2 = x.modPow(BigInteger.valueOf(3L), p).add(BigInteger.valueOf(7L)).mod(p);
            return y2;
        }

        private static BigInteger sqrtMod(final BigInteger y2) {
            var y = y2.modPow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4L)), p);
            return y;
        }
    }

    public static class Pair<K, V> {

        private K elementLeft = null;
        private V elementRight = null;

        protected Pair() {
        }

        public static <K, V> Pair<K, V> of(K elementLeft, V elementRight) {
            return new Pair<>(elementLeft, elementRight);
        }

        public Pair(K elementLeft, V elementRight) {
            this.elementLeft = elementLeft;
            this.elementRight = elementRight;
        }

        public K getLeft() {
            return elementLeft;
        }

        public V getRight() {
            return elementRight;
        }

        public boolean equals(Pair<K, V> p) {
            return (this.elementLeft.equals(p.getLeft())) && (this.elementRight.equals(p.getRight()));
        }

    }

    public static class NostrException extends Exception {

        public NostrException() {
            super();
        }

        public NostrException(String message) {
            super(message);
        }

        public NostrException(Throwable t) {
            super(t);
        }
    }

}
