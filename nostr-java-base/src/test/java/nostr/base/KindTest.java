package nostr.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KindTest {

  @Test
  void testValueOfValid() {
    Kind kind = Kind.valueOf(Kind.TEXT_NOTE.getValue());
    assertEquals(Kind.TEXT_NOTE, kind);
    assertEquals(Integer.toString(Kind.TEXT_NOTE.getValue()), kind.toString());
  }

  @Test
  void testValueOfUnknownReturnsTextNote() {
    Kind kind = Kind.valueOf(999);
    assertEquals(Kind.TEXT_NOTE, kind);
  }

  @Test
  void testValueOfInvalidRange() {
    assertThrows(IllegalArgumentException.class, () -> Kind.valueOf(70_000));
  }

  @Test
  void testEnumValues() {
    for (Kind k : Kind.values()) {
      assertNotNull(k);
    }
  }
}
