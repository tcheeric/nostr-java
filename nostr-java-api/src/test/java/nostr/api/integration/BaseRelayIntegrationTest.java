package nostr.api.integration;

import com.github.dockerjava.api.model.Ulimit;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Base class for Testcontainers-backed relay integration tests.
 *
 * <p>Uses strfry relay by default. Configure via relay-container.properties.
 *
 * <p>Disabled automatically when the system property `noDocker=true` is set (e.g. CI without Docker).
 */
@DisabledIfSystemProperty(named = "noDocker", matches = "true")
@Testcontainers
public abstract class BaseRelayIntegrationTest {

  private static final String RESOURCE_BUNDLE = "relay-container";
  private static final String IMAGE_KEY = "relay.container.image";
  private static final String PORT_KEY = "relay.container.port";
  private static final int DEFAULT_PORT = 7777;

  private static final int relayPort;

  @Container private static final GenericContainer<?> RELAY;

  static {
    ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
    String image = bundle.getString(IMAGE_KEY);
    relayPort = bundle.containsKey(PORT_KEY)
        ? Integer.parseInt(bundle.getString(PORT_KEY))
        : DEFAULT_PORT;

    RELAY =
        new GenericContainer<>(image)
            .withExposedPorts(relayPort)
            .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig()
                .withUlimits(new Ulimit[] {new Ulimit("nofile", 1000000L, 1000000L)}))
            .withClasspathResourceMapping(
                "strfry.conf", "/etc/strfry.conf", BindMode.READ_ONLY)
            .withTmpFs(Map.of("/app/strfry-db", "rw"))
            .waitingFor(
                Wait.forLogMessage(".*Started websocket server on.*", 1)
                    .withStartupTimeout(Duration.ofSeconds(30)));
  }

  private static String relayUri;

  @BeforeAll
  static void ensureDockerAvailable() {
    Assumptions.assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Docker is required to run relay container");
    String host = RELAY.getHost();
    relayUri = String.format("ws://%s:%d", host, RELAY.getMappedPort(relayPort));
  }

  @DynamicPropertySource
  static void registerRelayProperties(DynamicPropertyRegistry registry) {
    String host = RELAY.getHost();
    relayUri = String.format("ws://%s:%d", host, RELAY.getMappedPort(relayPort));
    registry.add("relays.nostr_rs_relay", () -> relayUri);
  }

  static String getRelayUri() {
    return relayUri;
  }

  /**
   * Returns a relay map containing the Testcontainers relay URI.
   * Use this instead of autowired relays to ensure tests use the dynamic container port.
   */
  static Map<String, String> getTestRelays() {
    return Map.of("nostr_rs_relay", relayUri);
  }
}
