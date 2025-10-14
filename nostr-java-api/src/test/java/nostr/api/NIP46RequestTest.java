package nostr.api;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NIP46RequestTest {

  // Ensures params can be added and queried reliably.
  @Test
  void addAndQueryParams() {
    NIP46.Request req = new NIP46.Request("id-1", "sign_event", Set.of("a"));
    req.addParam("b");
    assertEquals(2, req.getParamCount());
    assertTrue(req.containsParam("a"));
    assertTrue(req.containsParam("b"));
    assertFalse(req.containsParam("c"));
  }
}

