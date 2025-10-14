package nostr.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RelayUriTest {
  // Accept only ws/wss schemes.
  @Test
  void validSchemes() {
    assertDoesNotThrow(() -> new RelayUri("ws://example"));
    assertDoesNotThrow(() -> new RelayUri("wss://example"));
  }

  // Reject non-websocket schemes.
  @Test
  void invalidScheme() {
    assertThrows(IllegalArgumentException.class, () -> new RelayUri("http://example"));
  }
}

