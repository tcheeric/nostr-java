package nostr.test.event;

import nostr.event.Kind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void testKindValueOfUndefined() {
    assertEquals("-1", Kind.valueOf(9999999).toString());
  }

  @Test
  void testKindUndefinedName() {
    assertEquals("undefined", Kind.valueOf(9999999).getName());
  }
}