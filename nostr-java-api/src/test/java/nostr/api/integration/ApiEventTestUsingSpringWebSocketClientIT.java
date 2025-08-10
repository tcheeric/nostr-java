package nostr.api.integration;

import lombok.SneakyThrows;
import nostr.api.NIP15;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.config.RelayConfig;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nostr.api.integration.ApiEventIT.createProduct;
import static nostr.api.integration.ApiEventIT.createStall;
import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(RelayConfig.class)
@ActiveProfiles("test")
class ApiEventTestUsingSpringWebSocketClientIT extends BaseRelayIntegrationTest {
    private final List<SpringWebSocketClient> springWebSocketClients;

    @Autowired
    public ApiEventTestUsingSpringWebSocketClientIT(Map<String, String> relays) {
        this.springWebSocketClients = relays.values().stream()
            .map(uri -> {
                try {
                    return new SpringWebSocketClient(new StandardWebSocketClient(uri), uri);
                } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            })
            .toList();
    }

    @Test
    void doForEach() {
        springWebSocketClients.forEach(this::testNIP15SendProductEventUsingSpringWebSocketClient);
    }

    @SneakyThrows
    void testNIP15SendProductEventUsingSpringWebSocketClient(SpringWebSocketClient springWebSocketClient) {
        System.out.println("testNIP15CreateProductEventUsingSpringWebSocketClient");
        var product = createProduct(createStall());

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        var nip15 = new NIP15(Identity.create(PrivateKey.generateRandomPrivKey()));

        GenericEvent event = nip15.createCreateOrUpdateProductEvent(product, categories).sign().getEvent();
        EventMessage message = new EventMessage(event);

        try (SpringWebSocketClient client = springWebSocketClient) {
            String eventResponse = client.send(message).stream().findFirst().orElseThrow();

            // Extract and compare only first 3 elements of the JSON array
            var expectedArray = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(0).asText();
            var expectedSubscriptionId = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(1).asText();
            var expectedSuccess = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(2).asBoolean();

            var actualArray = MAPPER_AFTERBURNER.readTree(eventResponse).get(0).asText();
            var actualSubscriptionId = MAPPER_AFTERBURNER.readTree(eventResponse).get(1).asText();
            var actualSuccess = MAPPER_AFTERBURNER.readTree(eventResponse).get(2).asBoolean();

            assertEquals(expectedArray, actualArray, "First element should match");
            assertEquals(expectedSubscriptionId, actualSubscriptionId, "Subscription ID should match");
            assertEquals(expectedSuccess, actualSuccess, "Success flag should match");
        }
    }

    private String expectedResponseJson(String sha256) {
        return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
    }
}
