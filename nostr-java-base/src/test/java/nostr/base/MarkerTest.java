package nostr.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MarkerTest {

  @Test
  void testGetValue() {
    for (Marker m : Marker.values()) {
      assertNotNull(m.getValue());
      assertFalse(m.getValue().isEmpty());
    }
  }
}
