package nostr.api.integration;

import nostr.api.NostrSpringWebSocketClient;
import nostr.api.integration.support.FakeWebSocketClient;
import nostr.api.integration.support.FakeWebSocketClientFactory;
import nostr.api.service.impl.DefaultNoteService;
import nostr.base.Kind;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for subscription lifecycle using a fake WebSocket client.
 */
public class SubscriptionLifecycleIT {

  /**
   * Validates that subscription listeners receive messages emitted by all relays.
   */
  @Test
  void testSubscriptionReceivesNewEvents() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays =
        Map.of(
            "relay1", "wss://relay1.example.com",
            "relay2", "wss://relay2.example.com");
    client.setRelays(relays);

    List<String> received = new CopyOnWriteArrayList<>();
    AutoCloseable handle =
        client.subscribe(new Filters(new KindFilter<>(Kind.TEXT_NOTE)), "sub-evt", received::add);
    try {
      // Simulate inbound events from both relays
      factory.get("wss://relay1.example.com").emit("EVENT from relay1");
      factory.get("wss://relay2.example.com").emit("EVENT from relay2");

      // Both messages should be received
      assertTrue(received.stream().anyMatch(s -> s.contains("relay1")));
      assertTrue(received.stream().anyMatch(s -> s.contains("relay2")));
    } finally {
      handle.close();
    }
  }

  /**
   * Validates concurrent subscriptions receive their respective messages without interference.
   */
  @Test
  void testConcurrentSubscriptions() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays =
        Map.of(
            "relay1", "wss://relay1.example.com",
            "relay2", "wss://relay2.example.com");
    client.setRelays(relays);

    List<String> s1 = new CopyOnWriteArrayList<>();
    List<String> s2 = new CopyOnWriteArrayList<>();

    AutoCloseable h1 = client.subscribe(new Filters(new KindFilter<>(Kind.TEXT_NOTE)), "sub-A", s1::add);
    AutoCloseable h2 = client.subscribe(new Filters(new KindFilter<>(Kind.TEXT_NOTE)), "sub-B", s2::add);
    try {
      factory.get("wss://relay1.example.com").emit("[\"EVENT\",\"sub-A\",{}]");
      factory.get("wss://relay2.example.com").emit("[\"EVENT\",\"sub-B\",{}]");

      assertTrue(s1.stream().anyMatch(m -> m.contains("sub-A")));
      assertTrue(s2.stream().anyMatch(m -> m.contains("sub-B")));
    } finally {
      h1.close();
      h2.close();
    }
  }

  /**
   * Errors emitted by the underlying client should propagate to the provided error listener.
   */
  @Test
  void testErrorPropagationToListener() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays = Map.of("relay", "wss://relay.example.com");
    client.setRelays(relays);

    List<Throwable> errors = new CopyOnWriteArrayList<>();
    AutoCloseable handle =
        client.subscribe(
            new Filters(new KindFilter<>(Kind.TEXT_NOTE)),
            "sub-err",
            m -> {},
            errors::add);
    try {
      factory.get("wss://relay.example.com").emitError(new RuntimeException("x"));
      assertTrue(errors.stream().anyMatch(e -> "x".equals(e.getMessage())));
    } finally {
      handle.close();
    }
  }

  /**
   * Subscribing without an explicit error listener should use a safe default and not throw when
   * errors occur.
   */
  @Test
  void testSubscribeWithoutErrorListenerUsesSafeDefault() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays = Map.of("relay", "wss://relay.example.com");
    client.setRelays(relays);

    AutoCloseable handle =
        client.subscribe(new Filters(new KindFilter<>(Kind.TEXT_NOTE)), "sub-safe", m -> {});
    try {
      // Emit an error; should be handled by safe default error consumer, not rethrown
      factory.get("wss://relay.example.com").emitError(new RuntimeException("err-safe"));
      assertTrue(true);
    } finally {
      handle.close();
    }
  }

  /**
   * Confirms that EOSE markers propagate to listeners as regular messages.
   */
  @Test
  void testEOSEMarkerReceived() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays = Map.of("relay", "wss://relay.example.com");
    client.setRelays(relays);

    List<String> received = new ArrayList<>();
    AutoCloseable handle =
        client.subscribe(new Filters(new KindFilter<>(Kind.TEXT_NOTE)), "sub-eose", received::add);
    try {
      factory.get("wss://relay.example.com").emit("[\"EOSE\",\"sub-eose\"]");
      assertTrue(received.stream().anyMatch(s -> s.contains("EOSE")));
    } finally {
      handle.close();
    }
  }

  /**
   * Ensures cancellation closes underlying subscription and sends CLOSE frame.
   */
  @Test
  void testCancelSubscriptionSendsClose() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    Map<String, String> relays = Map.of("relay", "wss://relay.example.com");
    client.setRelays(relays);

    AutoCloseable handle =
        client.subscribe(new Filters(new KindFilter<>(Kind.TEXT_NOTE)), "sub-close", s -> {});
    FakeWebSocketClient fake = factory.get("wss://relay.example.com");
    try {
      handle.close();
    } finally {
      // Verify a CLOSE message was sent (subscribe called with CLOSE frame)
      assertTrue(
          fake.getSentPayloads().stream().anyMatch(p -> p.contains("\"CLOSE\",\"sub-close\"")),
          "Close frame should be sent for subscription id");
    }
  }
}
