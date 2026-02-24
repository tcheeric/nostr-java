package nostr.crypto;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}

