package nostr.api.integration;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import java.util.ResourceBundle;

public abstract class BaseRelayIntegrationTest {
    @BeforeAll
    static void ensureRelayAvailable() {
        Assumptions.assumeTrue(RelayAvailability.areRelaysAvailable(),
                "Requires running relay defined in relays.properties");
    }

    protected String getRelayUri() {
        ResourceBundle bundle = ResourceBundle.getBundle("relays");
        return bundle.getString(bundle.keySet().iterator().next());
    }
}
