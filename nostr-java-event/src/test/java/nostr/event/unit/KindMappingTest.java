package nostr.event.unit;

import nostr.event.Kind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KindMappingTest {
  @Test
  void testKindValueOf() {
    assertEquals("1", Kind.valueOf(1).toString());
  }

  @Test
  void testKindName() {
    assertEquals("text_note", Kind.valueOf(1).getName());
  }

  @Test
  void testKindUndefinedName() {
    assertThrows(IllegalArgumentException.class, () -> Kind.valueOf(9999999));
  }
}
