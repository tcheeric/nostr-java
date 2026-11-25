package nostr.crypto;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import nostr.util.NostrUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PointTest {

  // Verifies constructor order (x,y) maps to getX/getY correctly.
  @Test
  void constructorOrderMatchesAccessors() {
    BigInteger x = new BigInteger("12345678901234567890");
    BigInteger y = new BigInteger("98765432109876543210");
    Point p = new Point(x, y);
    assertEquals(x, p.getX());
    assertEquals(y, p.getY());
  }

  // Ensures infinityPoint produces an infinite point.
  @Test
  void infinityPointIsInfinite() {
    Point inf = Point.infinityPoint();
    assertTrue(inf.isInfinite());
  }

  // Known value: x-coordinate of 2G on secp256k1.
  @Test
  void mulMatchesKnownMultiplesOfG() {
    assertSamePoint(Point.G, Point.mul(Point.G, BigInteger.ONE));
    Point twoG = Point.mul(Point.G, BigInteger.TWO);
    assertEquals(
        "c6047f9441ed7d6d3045406e95c07cd85c778e4b8cef3ca7abac09b95c709ee5",
        NostrUtil.bytesToHex(NostrUtil.bytesFromBigInteger(twoG.getX())));
    assertSamePoint(twoG, Point.add(Point.G, Point.G));
  }

  // The group order times G is the point at infinity; a zero scalar yields no point.
  @Test
  void mulByOrderIsInfinityAndByZeroIsNull() {
    assertTrue(Point.mul(Point.G, Point.getn()).isInfinite());
    assertNull(Point.mul(Point.G, BigInteger.ZERO));
  }

  // The simultaneous multiplication must equal two multiplications plus an addition.
  @Test
  void mulSumEqualsSeparateMultiplications() {
    Point q = Point.mul(Point.G, new BigInteger("123456789012345678901234567890"));
    BigInteger a = new BigInteger("987654321098765432109876543210987654321");
    BigInteger b = Point.getn().subtract(BigInteger.valueOf(77));
    assertSamePoint(
        Point.add(Point.mul(Point.G, a), Point.mul(q, b)), Point.mulSum(Point.G, a, q, b));
    assertSamePoint(Point.add(Point.mul(q, a), Point.mul(q, b)), Point.mulSum(q, a, q, b));
  }

  // liftX keeps the even y, rejects out-of-range x, and rejects x with no curve point.
  @Test
  void liftXReturnsEvenYPointsOnTheCurveAndRejectsTheRest() {
    assertSamePoint(Point.G, Point.liftX(NostrUtil.bytesFromBigInteger(Point.G.getX())));
    assertNull(Point.liftX(NostrUtil.bytesFromBigInteger(Point.getp())));
    int lifted = 0;
    int rejected = 0;
    for (int i = 1; i <= 40; i++) {
      Point lift = Point.liftX(NostrUtil.bytesFromBigInteger(BigInteger.valueOf(i)));
      if (lift == null) {
        rejected++;
        continue;
      }
      lifted++;
      assertTrue(lift.hasEvenY());
      BigInteger x = lift.getX();
      BigInteger rhs = x.pow(3).add(BigInteger.valueOf(7)).mod(Point.getp());
      assertEquals(rhs, lift.getY().pow(2).mod(Point.getp()));
    }
    assertTrue(lifted > 0 && rejected > 0);
  }

  // Point defines only equals(Point), an overload, so compare coordinates explicitly.
  private static void assertSamePoint(Point expected, Point actual) {
    assertEquals(expected.getX(), actual.getX());
    assertEquals(expected.getY(), actual.getY());
  }
}
