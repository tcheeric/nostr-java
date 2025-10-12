package nostr.event.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for EventTypeChecker ranges and naming. */
public class EventTypeCheckerTest {

  @Test
  void replacesEphemeralAddressableRegular() {
    assertTrue(EventTypeChecker.isReplaceable(10000));
    assertTrue(EventTypeChecker.isEphemeral(20000));
    assertTrue(EventTypeChecker.isAddressable(30000));
    assertTrue(EventTypeChecker.isRegular(1));
    assertEquals("replaceable", EventTypeChecker.getTypeName(10001));
    assertEquals("ephemeral", EventTypeChecker.getTypeName(20001));
    assertEquals("addressable", EventTypeChecker.getTypeName(30001));
    assertEquals("regular", EventTypeChecker.getTypeName(40000));
  }

  @Test
  void utilityClassConstructorThrows() {
    assertThrows(UnsupportedOperationException.class, () -> {
      var c = EventTypeChecker.class.getDeclaredConstructors()[0];
      c.setAccessible(true);
      try { c.newInstance(); } catch (ReflectiveOperationException e) {
        Throwable cause = e.getCause();
        if (cause instanceof UnsupportedOperationException uoe) throw uoe;
        throw new RuntimeException(e);
      }
    });
  }
}

