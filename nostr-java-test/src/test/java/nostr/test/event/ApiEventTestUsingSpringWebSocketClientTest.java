package nostr.test.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.api.NIP15;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import nostr.test.util.JsonComparator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nostr.test.event.ApiEventTest.createProduct;
import static nostr.test.event.ApiEventTest.createStall;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiEventTestUsingSpringWebSocketClientTest {
  private static Map<String, String> relays;
  private SpringWebSocketClient springWebSocketClient;

  @BeforeAll
  static void setupBeforeAll() {
    relays = ApiEventTest.getRelays();
  }

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
    EventMessage message = new EventMessage(event, event.getId());

    ObjectMapper mapper = new ObjectMapper();

    assertTrue(
        JsonComparator.isEquivalentJson(
            mapper.readTree(expectedResponseJson(event.getId())),
            mapper.readTree(springWebSocketClient.send(message).stream().findFirst().get())));

    springWebSocketClient.closeSocket();
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }
}
