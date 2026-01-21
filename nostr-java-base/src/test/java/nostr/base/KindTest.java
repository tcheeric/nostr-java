package nostr.base;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KindTest {

  @Test
  void testValueOfValid() {
    Kind kind = Kind.valueOf(Kind.TEXT_NOTE.getValue());
    assertEquals(Kind.TEXT_NOTE, kind);
    assertEquals(Integer.toString(Kind.TEXT_NOTE.getValue()), kind.toString());
  }

  @Test
  void testValueOfUnknownReturnsNull() {
    Kind kind = Kind.valueOf(999);
    assertNull(kind, "Unknown kind values should return null for lenient handling");
  }

  @Test
  void testValueOfInvalidRange() {
    assertThrows(IllegalArgumentException.class, () -> Kind.valueOf(70_000));
  }

  @Test
  void testValueOfStrictValid() {
    Kind kind = Kind.valueOfStrict(Kind.REACTION.getValue());
    assertEquals(Kind.REACTION, kind);
  }

  @Test
  void testValueOfStrictUnknownThrows() {
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> Kind.valueOfStrict(999)
    );
    assertTrue(ex.getMessage().contains("999"));
  }

  @Test
  void testValueOfStrictInvalidRangeThrows() {
    assertThrows(IllegalArgumentException.class, () -> Kind.valueOfStrict(70_000));
  }

  @Test
  void testFindByValueValid() {
    Optional<Kind> kind = Kind.findByValue(Kind.ZAP_RECEIPT.getValue());
    assertTrue(kind.isPresent());
    assertEquals(Kind.ZAP_RECEIPT, kind.get());
  }

  @Test
  void testFindByValueUnknownReturnsEmpty() {
    Optional<Kind> kind = Kind.findByValue(999);
    assertTrue(kind.isEmpty(), "Unknown kind should return empty Optional");
  }

  @Test
  void testFindByValueInvalidRangeReturnsEmpty() {
    Optional<Kind> kind = Kind.findByValue(70_000);
    assertTrue(kind.isEmpty(), "Out of range kind should return empty Optional");
  }

  @Test
  void testEnumValues() {
    for (Kind k : Kind.values()) {
      assertNotNull(k);
    }
  }
}
