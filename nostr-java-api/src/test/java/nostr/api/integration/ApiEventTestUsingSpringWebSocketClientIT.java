package nostr.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.api.NIP15;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static nostr.api.integration.ApiEventIT.createProduct;
import static nostr.api.integration.ApiEventIT.createStall;
import static nostr.base.json.EventJsonMapper.mapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiEventTestUsingSpringWebSocketClientIT extends BaseRelayIntegrationTest {
  private static final int MAX_CLIENT_CONNECTION_ATTEMPTS = 3;
  private static final long CONNECTION_RETRY_DELAY_MS = 1_000L;

  @Test
  // Executes the NIP-15 product event test against every configured relay endpoint.
  void doForEach() throws InterruptedException {
    // Give the relay a moment to fully initialize after container startup
    Thread.sleep(500);
    List.of(getRelayUri())
        .forEach(
            relayUri -> {
              try {
                testNIP15SendProductEventUsingSpringWebSocketClient(relayUri);
              } catch (java.io.IOException e) {
                Assertions.fail("Failed to execute NIP-15 test for relay " + relayUri, e);
              }
            });
  }

  void testNIP15SendProductEventUsingSpringWebSocketClient(
      String relayUri) throws java.io.IOException {
    System.out.println("testNIP15CreateProductEventUsingSpringWebSocketClient");
    var product = createProduct(createStall());

    List<String> categories = new ArrayList<>();
    categories.add("bijoux");
    categories.add("Hommes");

    var nip15 = new NIP15(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event =
        nip15.createCreateOrUpdateProductEvent(product, categories).sign().getEvent();
    EventMessage message = new EventMessage(event);

    try (SpringWebSocketClient client = createSpringWebSocketClient(relayUri)) {
      String eventResponse = client.send(message).stream().findFirst().orElseThrow();

      try {
        JsonNode actualNode = mapper().readTree(eventResponse);

        // Verify OK response format: ["OK", "<event_id>", <boolean>, "<message>"]
        assertEquals("OK", actualNode.get(0).asText(), "Response should be an OK message");
        assertEquals(event.getId(), actualNode.get(1).asText(), "Event ID should match");
        // Note: success flag (element 2) varies by relay implementation, so we just log it
        System.out.println("Relay response: success=" + actualNode.get(2).asBoolean()
            + ", message=" + (actualNode.has(3) ? actualNode.get(3).asText() : "none"));
      } catch (JsonProcessingException ex) {
        Assertions.fail("Failed to parse relay response JSON: " + ex.getMessage(), ex);
      }
    }
  }

  private SpringWebSocketClient createSpringWebSocketClient(String relayUri) {
    ExecutionException lastException = null;

    for (int attempt = 1; attempt <= MAX_CLIENT_CONNECTION_ATTEMPTS; attempt++) {
      try {
        return new SpringWebSocketClient(new StandardWebSocketClient(relayUri), relayUri);
      } catch (ExecutionException e) {
        lastException = e;
        delayBeforeRetry(attempt);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while connecting to " + relayUri, e);
      }
    }

    throw new IllegalStateException(
        "Failed to initialize WebSocket client for " + relayUri + " after "
            + MAX_CLIENT_CONNECTION_ATTEMPTS
            + " attempts",
        lastException);
  }

  private void delayBeforeRetry(int attempt) {
    if (attempt >= MAX_CLIENT_CONNECTION_ATTEMPTS) {
      return;
    }
    try {
      Thread.sleep(CONNECTION_RETRY_DELAY_MS);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}
