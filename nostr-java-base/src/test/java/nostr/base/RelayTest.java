package nostr.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelayTest {

  @Test
  void testRelayHelpers() {
    Relay.RelayInformationDocument doc =
        Relay.RelayInformationDocument.builder().name("test").build();
    Relay relay = new Relay("ws://relay.example.com", doc);

    assertEquals("ws://relay.example.com", relay.getUri());
    assertEquals("http://relay.example.com", relay.getHttpUri());
    assertEquals("test", relay.getName());

    relay.addNipSupport(1);
    assertTrue(relay.getSupportedNips().contains(1));
  }
}
