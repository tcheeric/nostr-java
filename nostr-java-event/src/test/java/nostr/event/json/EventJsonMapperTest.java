package nostr.event.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Tests for EventJsonMapper contract. */
public class EventJsonMapperTest {

  @Test
  void getMapperReturnsSingleton() {
    ObjectMapper m1 = EventJsonMapper.getMapper();
    ObjectMapper m2 = EventJsonMapper.getMapper();
    assertSame(m1, m2);
  }

  @Test
  void constructorIsInaccessible() {
    assertThrows(UnsupportedOperationException.class, () -> {
      var c = EventJsonMapper.class.getDeclaredConstructors()[0];
      c.setAccessible(true);
      try { c.newInstance(); } catch (ReflectiveOperationException e) {
        // unwrap
        Throwable cause = e.getCause();
        if (cause instanceof UnsupportedOperationException uoe) throw uoe;
        throw new RuntimeException(e);
      }
    });
  }
}

