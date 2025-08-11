package nostr.crypto;

import lombok.NonNull;
import nostr.util.NostrUtil;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class Point {

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

    public Point add(@NonNull Point P) {
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

        byte[] tagHash = NostrUtil.sha256(tag.getBytes());
        int len = (tagHash.length * 2) + msg.length;
        byte[] buf = new byte[len];
        System.arraycopy(tagHash, 0, buf, 0, tagHash.length);
        System.arraycopy(tagHash, 0, buf, tagHash.length, tagHash.length);
        System.arraycopy(msg, 0, buf, tagHash.length * 2, msg.length);

        return NostrUtil.sha256(buf);
    }

    public byte[] toBytes() {
        return bytesFromPoint(this);
    }

    public static byte[] bytesFromPoint(Point P) {
        return NostrUtil.bytesFromBigInteger(P.getX());
    }

    // previously 'pointFromBytes()'
    public static Point liftX(byte[] b) {

        BigInteger x = NostrUtil.bigIntFromBytes(b);
        if (x.compareTo(p) >= 0) {
            return null;
        }
        BigInteger y_sq = x.modPow(BigInteger.valueOf(3L), p).add(BigInteger.valueOf(7L)).mod(p);
        BigInteger y = y_sq.modPow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4L)), p);

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
}
