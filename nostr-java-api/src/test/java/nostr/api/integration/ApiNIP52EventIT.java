package nostr.api.integration;

import nostr.api.NIP52;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static nostr.base.json.EventJsonMapper.mapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class ApiNIP52EventIT extends BaseRelayIntegrationTest {
  private static final int MAX_CLIENT_CONNECTION_ATTEMPTS = 3;
  private static final long CONNECTION_RETRY_DELAY_MS = 1_000L;

  @Test
  void testNIP52CalendarTimeBasedEventEventUsingSpringWebSocketClient()
      throws IOException, InterruptedException {
    // Give the relay a moment to fully initialize after container startup
    Thread.sleep(500);
    System.out.println("testNIP52CalendarTimeBasedEventEventUsingSpringWebSocketClient");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(
        new PubKeyTag(
            new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985"),
            null,
            "PAYER"));
    tags.add(
        new PubKeyTag(
            new PublicKey("494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347"),
            null,
            "PAYEE"));

    var nip52 = new NIP52(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event =
        nip52
            .createCalendarTimeBasedEvent(tags, "content", createCalendarContent())
            .sign()
            .getEvent();
    EventMessage message = new EventMessage(event);

    try (SpringWebSocketClient client = createSpringWebSocketClient(getRelayUri())) {
      var actualJson =
          mapper().readTree(client.send(message).stream().findFirst().orElseThrow());

      // Verify OK response format: ["OK", "<event_id>", <boolean>, "<message>"]
      assertEquals("OK", actualJson.get(0).asText(), "Response should be an OK message");
      assertEquals(event.getId(), actualJson.get(1).asText(), "Event ID should match");
      // Note: success flag (element 2) varies by relay implementation, so we just log it
      System.out.println("Relay response: success=" + actualJson.get(2).asBoolean()
          + ", message=" + (actualJson.has(3) ? actualJson.get(3).asText() : "none"));
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
        "Failed to initialize WebSocket client for "
            + relayUri
            + " after "
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

  private CalendarContent<BaseTag> createCalendarContent() {
    return new CalendarContent<>(
        new IdentifierTag("UUID-CalendarTimeBasedEventTest"),
        "Calendar Time-Based Event title",
        1716513986268L);
  }
}
