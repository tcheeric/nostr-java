package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.api.NIP15;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static nostr.test.event.ApiEventTest.createProduct;
import static nostr.test.event.ApiEventTest.createStall;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiEventTestUsingSpringWebSocketClientTest {
  private static final String RELAY_URI = "ws://localhost:5555";
  private final SpringWebSocketClient springWebSocketClient;

  public ApiEventTestUsingSpringWebSocketClientTest() {
    springWebSocketClient = new SpringWebSocketClient(RELAY_URI);
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

    assertEquals(
        expectedResponseJson(event.getId()),
        springWebSocketClient.send(message).stream().findFirst().get());

    springWebSocketClient.closeSocket();
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }
}
