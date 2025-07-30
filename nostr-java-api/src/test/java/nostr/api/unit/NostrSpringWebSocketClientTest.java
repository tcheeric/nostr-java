package nostr.api.unit;

import nostr.api.NostrIF;
import nostr.api.NostrSpringWebSocketClient;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertSame;

public class NostrSpringWebSocketClientTest {

    @BeforeEach
    void resetSingleton() throws Exception {
        Field instance = NostrSpringWebSocketClient.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void getInstanceShouldReturnSameInstance() {
        NostrIF first = NostrSpringWebSocketClient.getInstance();
        NostrIF second = NostrSpringWebSocketClient.getInstance();
        assertSame(first, second, "Multiple calls should return the same instance");
    }

    @Test
    void getInstanceWithIdentityShouldReturnSameInstance() {
        Identity identity = Identity.generateRandomIdentity();
        NostrIF first = NostrSpringWebSocketClient.getInstance(identity);
        NostrIF second = NostrSpringWebSocketClient.getInstance();
        assertSame(first, second, "Calls with and without identity should return the same instance");
    }
}
