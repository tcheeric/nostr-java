package nostr.base;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CommandTest {

  @Test
  void testEnumValues() {
    for (Command c : Command.values()) {
      assertNotNull(c);
    }
  }
}
