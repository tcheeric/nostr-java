package nostr.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommandTest {

    @Test
    void testEnumValues() {
        for (Command c : Command.values()) {
            assertNotNull(c);
        }
    }
}
