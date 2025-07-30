package nostr.api.integration;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.ResourceBundle;

@Testcontainers
public abstract class BaseRelayIntegrationTest {

    private static final int RELAY_PORT = 8080;

    private static final String RESOURCE_BUNDLE = "relay-container";
    private static final String IMAGE_KEY = "relay.container.image";

    @Container
    private static final GenericContainer<?> RELAY;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
        String image = bundle.getString(IMAGE_KEY);
        RELAY = new GenericContainer<>(image)
                .withExposedPorts(RELAY_PORT)
                .waitingFor(Wait.forListeningPort());
    }

    private static String relayUri;

    @BeforeAll
    static void ensureDockerAvailable() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker is required to run nostr-rs-relay container");
    }

    @DynamicPropertySource
    static void registerRelayProperties(DynamicPropertyRegistry registry) {
        relayUri = String.format("ws://localhost:%d", RELAY.getMappedPort(RELAY_PORT));
        registry.add("relays.nostr_rs_relay", () -> relayUri);
    }

    static String getRelayUri() {
        return relayUri;
    }
}
