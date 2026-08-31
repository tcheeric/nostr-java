package nostr.client.relay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the de-duplication window suppresses repeats without growing without limit. */
class DeliveredEventWindowTest {

  // Verifies the first sighting of an identifier is reported as new and later ones are not,
  // which is what suppresses the same event arriving from several relays.
  @Test
  void onlyTheFirstSightingOfAnIdentifierIsNew() {
    DeliveredEventWindow window = new DeliveredEventWindow(8);

    assertTrue(window.isFirstSighting("event-a"));
    assertFalse(window.isFirstSighting("event-a"));
    assertTrue(window.isFirstSighting("event-b"));
  }

  // Verifies the window never holds more identifiers than its capacity, so a long-lived
  // subscription cannot grow its memory without limit.
  @Test
  void theWindowNeverExceedsItsCapacity() {
    int capacity = 16;
    DeliveredEventWindow window = new DeliveredEventWindow(capacity);

    for (int identifier = 0; identifier < capacity * 50; identifier++) {
      window.isFirstSighting("event-" + identifier);
    }

    assertEquals(capacity, window.size());
  }

  // Verifies an identifier pushed out of the window is treated as new again, which is the
  // accepted cost of bounding memory and must be visible rather than surprising.
  @Test
  void anEvictedIdentifierIsSeenAsNewAgain() {
    DeliveredEventWindow window = new DeliveredEventWindow(4);
    window.isFirstSighting("oldest");

    for (int identifier = 0; identifier < 8; identifier++) {
      window.isFirstSighting("filler-" + identifier);
    }

    assertTrue(window.isFirstSighting("oldest"));
  }

  // Verifies a window must have room for at least one identifier, since a zero-sized window
  // would silently disable de-duplication.
  @Test
  void aWindowMustHaveRoomForAtLeastOneIdentifier() {
    assertThrows(IllegalArgumentException.class, () -> new DeliveredEventWindow(0));
  }
}
