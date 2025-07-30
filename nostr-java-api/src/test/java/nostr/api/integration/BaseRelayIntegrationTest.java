package nostr.api.integration;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseRelayIntegrationTest {
    @BeforeAll
    static void ensureRelayAvailable() {
        Assumptions.assumeTrue(RelayAvailability.areRelaysAvailable(),
                "Requires running relay defined in relays.properties");
    }
}
