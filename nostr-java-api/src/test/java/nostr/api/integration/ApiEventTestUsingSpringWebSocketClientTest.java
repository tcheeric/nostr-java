package nostr.api.integration;

import nostr.api.NIP15;
import nostr.config.RelayProperties;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nostr.api.integration.ApiEventTest.createProduct;
import static nostr.api.integration.ApiEventTest.createStall;
import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(RelayProperties.class)
@ActiveProfiles("test")
class ApiEventTestUsingSpringWebSocketClientTest {
    private SpringWebSocketClient springWebSocketClient;

    @Autowired
    Map<String, String> relays;

    @BeforeEach
    void setupBeforeEach() {
        relays.forEach((key, value) -> springWebSocketClient = new SpringWebSocketClient(value));
    }

    @Test
    void testNIP15SendProductEventUsingSpringWebSocketClient() throws IOException {
        System.out.println("testNIP15CreateProductEventUsingSpringWebSocketClient");
        var product = createProduct(createStall());

        List<String> categories = new ArrayList<>();
        categories.add("bijoux");
        categories.add("Hommes");

        var nip15 = new NIP15<>(Identity.create(PrivateKey.generateRandomPrivKey()));

        GenericEvent event = nip15.createCreateOrUpdateProductEvent(product, categories).sign().getEvent();
        EventMessage message = new EventMessage(event);

        String eventResponse = springWebSocketClient.send(message).stream().findFirst().orElseThrow();

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

        springWebSocketClient.closeSocket();
    }

    private String expectedResponseJson(String sha256) {
        return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
    }
}
