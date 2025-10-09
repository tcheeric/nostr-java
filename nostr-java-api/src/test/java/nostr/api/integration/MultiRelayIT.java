package nostr.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import nostr.api.NostrSpringWebSocketClient;
import nostr.api.integration.support.FakeWebSocketClient;
import nostr.api.integration.support.FakeWebSocketClientFactory;
import nostr.api.service.impl.DefaultNoteService;
import nostr.base.Kind;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

/**
 * Integration tests covering multi-relay behavior using a fake WebSocket client factory.
 */
public class MultiRelayIT {

  /**
   * Verifies that sending an event broadcasts to all configured relays and returns responses from
   * each relay.
   */
  @Test
  void testBroadcastToMultipleRelays() {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays =
        Map.of(
            "relay1", "wss://relay1.example.com",
            "relay2", "wss://relay2.example.com",
            "relay3", "wss://relay3.example.com");
    client.setRelays(relays);

    GenericEvent event =
        GenericEvent.builder()
            .pubKey(sender.getPublicKey())
            .kind(Kind.TEXT_NOTE)
            .content("hello nostr")
            .build();
    event.update();
    client.sign(sender, event);

    List<String> responses = client.sendEvent(event);
    assertEquals(3, responses.size(), "Should receive one response per relay");
    assertTrue(responses.contains("OK:wss://relay1.example.com"));
    assertTrue(responses.contains("OK:wss://relay2.example.com"));
    assertTrue(responses.contains("OK:wss://relay3.example.com"));

    // Also check each fake recorded the payload
    for (String uri : relays.values()) {
      FakeWebSocketClient fake = factory.get(uri);
      assertTrue(
          fake.getSentPayloads().stream().anyMatch(p -> p.contains("EVENT")),
          "Relay should have been sent an EVENT message: " + uri);
    }
  }

  /**
   * Ensures that if one relay fails to send, other relay responses are still returned and
   * the failure is recorded for diagnostics.
   */
  @Test
  void testRelayFailoverReturnsAvailableResponses() {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    DefaultNoteService noteService = new DefaultNoteService();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, noteService, factory);

    Map<String, String> relays =
        Map.of(
            "relayA", "wss://relayA.example.com",
            "relayB", "wss://relayB.example.com");
    client.setRelays(relays);

    GenericEvent event =
        GenericEvent.builder()
            .pubKey(sender.getPublicKey())
            .kind(Kind.TEXT_NOTE)
            .content("broadcast with partial availability")
            .build();
    event.update();
    client.sign(sender, event);

    // Simulate relayB failure
    FakeWebSocketClient relayB = factory.get("wss://relayB.example.com");
    try { relayB.close(); } catch (Exception ignored) {}

    List<String> responses = client.sendEvent(event);
    assertEquals(1, responses.size());
    assertTrue(responses.contains("OK:wss://relayA.example.com"));

    Map<String, Throwable> failures = noteService.getLastFailures();
    assertTrue(failures.containsKey("relayB"));

    // Also visible via client accessors
    Map<String, Throwable> clientFailures = client.getLastSendFailures();
    assertTrue(clientFailures.containsKey("relayB"));

    // Structured details available as well
    var details = client.getLastSendFailureDetails();
    assertTrue(details.containsKey("relayB"));
  }

  /**
   * Verifies that a REQ is sent per relay and contains the subscription id.
   */
  @Test
  void testCrossRelayEventRetrievalViaReq() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays =
        Map.of(
            "relay1", "wss://relay1.example.com",
            "relay2", "wss://relay2.example.com");
    client.setRelays(relays);

    // Open a subscription (so request clients exist) and then send a REQ
    var received = new CopyOnWriteArrayList<String>();
    var handle =
        client.subscribe(
            new nostr.event.filter.Filters(new nostr.event.filter.KindFilter<>(Kind.TEXT_NOTE)),
            "sub-123",
            received::add);
    try {
      List<String> reqResponses =
          client.sendRequest(
              new nostr.event.filter.Filters(
                  new nostr.event.filter.KindFilter<>(Kind.TEXT_NOTE)),
              "sub-123");
      assertEquals(2, reqResponses.size());

      // Check REQ payloads captured by fakes
      for (String uri : relays.values()) {
        FakeWebSocketClient fake = factory.get(uri);
        assertTrue(
            fake.getSentPayloads().stream().anyMatch(p -> p.contains("\"REQ\",\"sub-123\"")),
            "Relay should have been sent a REQ for sub-123: " + uri);
      }
    } finally {
      handle.close();
    }
  }
}
