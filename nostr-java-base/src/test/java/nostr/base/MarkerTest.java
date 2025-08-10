package nostr.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarkerTest {

    @Test
    void testGetValue() {
        for (Marker m : Marker.values()) {
            assertNotNull(m.getValue());
            assertFalse(m.getValue().isEmpty());
        }
    }
}
