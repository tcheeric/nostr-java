package nostr.base;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MarkerTest {

  @Test
  void testGetValue() {
    for (Marker m : Marker.values()) {
      assertNotNull(m.getValue());
      assertFalse(m.getValue().isEmpty());
    }
  }
}
