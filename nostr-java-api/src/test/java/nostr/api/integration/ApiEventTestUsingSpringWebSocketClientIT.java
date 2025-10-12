package nostr.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.api.NIP15;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.config.RelayConfig;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nostr.api.integration.ApiEventIT.createProduct;
import static nostr.api.integration.ApiEventIT.createStall;
import static nostr.base.json.EventJsonMapper.mapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(RelayConfig.class)
@ActiveProfiles("test")
class ApiEventTestUsingSpringWebSocketClientIT extends BaseRelayIntegrationTest {
  private final List<SpringWebSocketClient> springWebSocketClients;

  @Autowired
  public ApiEventTestUsingSpringWebSocketClientIT(
      @Qualifier("relays") Map<String, String> relays) {
    this.springWebSocketClients =
        relays.values().stream()
            .map(
                uri -> {
                  try {
                    return new SpringWebSocketClient(new StandardWebSocketClient(uri), uri);
                  } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toList();
  }

  @Test
  // Executes the NIP-15 product event test against every configured relay endpoint.
  void doForEach() {
    springWebSocketClients.forEach(client -> {
      try {
        testNIP15SendProductEventUsingSpringWebSocketClient(client);
      } catch (java.io.IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  void testNIP15SendProductEventUsingSpringWebSocketClient(
      SpringWebSocketClient springWebSocketClient) throws java.io.IOException {
    System.out.println("testNIP15CreateProductEventUsingSpringWebSocketClient");
    var product = createProduct(createStall());

    List<String> categories = new ArrayList<>();
    categories.add("bijoux");
    categories.add("Hommes");

    var nip15 = new NIP15(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event =
        nip15.createCreateOrUpdateProductEvent(product, categories).sign().getEvent();
    EventMessage message = new EventMessage(event);

    try (SpringWebSocketClient client = springWebSocketClient) {
      String eventResponse = client.send(message).stream().findFirst().orElseThrow();

      try {
        JsonNode expectedNode = mapper().readTree(expectedResponseJson(event.getId()));
        JsonNode actualNode = mapper().readTree(eventResponse);

        assertEquals(expectedNode.get(0).asText(), actualNode.get(0).asText(),
            "First element should match");
        assertEquals(expectedNode.get(1).asText(), actualNode.get(1).asText(),
            "Subscription ID should match");
        assertEquals(expectedNode.get(2).asBoolean(), actualNode.get(2).asBoolean(),
            "Success flag should match");
      } catch (JsonProcessingException ex) {
        Assertions.fail("Failed to parse relay response JSON: " + ex.getMessage(), ex);
      }
    }
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }
}
