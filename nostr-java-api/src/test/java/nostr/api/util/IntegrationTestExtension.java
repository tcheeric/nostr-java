package nostr.api.util;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit extension that introduces a delay between integration tests.
 */
public class IntegrationTestExtension implements AfterEachCallback {

    private static final long DELAY_MILLIS = 5000;

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        try {
            Thread.sleep(DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}