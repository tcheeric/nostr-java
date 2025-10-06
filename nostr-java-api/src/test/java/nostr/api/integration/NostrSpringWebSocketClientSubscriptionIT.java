package nostr.api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.NonNull;
import nostr.api.NostrSpringWebSocketClient;
import nostr.api.TestableWebSocketClientHandler;
import nostr.api.WebSocketClientHandler;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.WebSocketClientIF;
import nostr.event.BaseMessage;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.base.Kind;
import org.junit.jupiter.api.Test;

class NostrSpringWebSocketClientSubscriptionIT {

  // Ensures that long-lived subscriptions stream events and send CLOSE frames on cancellation.
  @Test
  void subscriptionStreamsAndClosesCleanly() throws Exception {
    RecordingNostrClient client = new RecordingNostrClient();
    client.setRelays(Map.of("relay-a", "ws://relay"));

    Filters filters = new Filters(new KindFilter<>(Kind.TEXT_NOTE));
    List<String> received = new ArrayList<>();
    List<Throwable> errors = new ArrayList<>();

    AutoCloseable handle =
        client.subscribe(filters, "sub-123", received::add, errors::add);

    RecordingHandler handler = client.getHandler("relay-a");
    StubWebSocketClient stub = handler.getSubscriptionClient("sub-123");
    assertFalse(stub.isClosed());
    assertTrue(stub.getSentMessages().getFirst().contains("REQ"));

    stub.emit("event-1");
    Thread.sleep(10L);
    stub.emit("event-2");

    assertEquals(List.of("event-1", "event-2"), received);
    assertTrue(errors.isEmpty());

    handle.close();
    assertTrue(stub.isClosed());
    assertTrue(stub.getSentMessages().stream().anyMatch(payload -> payload.contains("CLOSE")));

    stub.emit("event-3");
    assertEquals(2, received.size());
  }

  private static final class RecordingNostrClient extends NostrSpringWebSocketClient {
    private final Map<String, RecordingHandler> handlers = new ConcurrentHashMap<>();

    @Override
    protected WebSocketClientHandler newWebSocketClientHandler(String relayName, nostr.base.RelayUri relayUri) {
      RecordingHandler handler = new RecordingHandler(relayName, relayUri.toString());
      handlers.put(relayName, handler);
      return handler;
    }

    RecordingHandler getHandler(String relayName) {
      return handlers.get(relayName);
    }
  }

  private static final class RecordingHandler extends TestableWebSocketClientHandler {
    private final Map<String, StubWebSocketClient> subscriptionClients;

    RecordingHandler(String relayName, String relayUri) {
      this(relayName, relayUri, new ConcurrentHashMap<>());
    }

    private RecordingHandler(
        String relayName, String relayUri, Map<String, StubWebSocketClient> subscriptionClients) {
      super(
          relayName,
          relayUri,
          new SpringWebSocketClient(new StubWebSocketClient(), relayUri),
          key -> {
            StubWebSocketClient stub = new StubWebSocketClient();
            subscriptionClients.put(key, stub);
            return new SpringWebSocketClient(stub, relayUri);
          });
      this.subscriptionClients = subscriptionClients;
    }

    StubWebSocketClient getSubscriptionClient(String subscriptionId) {
      return subscriptionClients.get(subscriptionId);
    }
  }

  private static final class StubWebSocketClient implements WebSocketClientIF {
    private final List<String> sentMessages = new CopyOnWriteArrayList<>();
    private final Map<String, Consumer<String>> messageListeners = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Throwable>> errorListeners = new ConcurrentHashMap<>();
    private final Map<String, Runnable> closeListeners = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public <T extends BaseMessage> List<String> send(@NonNull T eventMessage) throws IOException {
      return send(eventMessage.encode());
    }

    @Override
    public List<String> send(String json) {
      sentMessages.add(json);
      return List.of();
    }

    @Override
    public AutoCloseable subscribe(
        String requestJson,
        Consumer<String> messageListener,
        Consumer<Throwable> errorListener,
        Runnable closeListener)
        throws IOException {
      String id = UUID.randomUUID().toString();
      sentMessages.add(requestJson);
      messageListeners.put(id, messageListener);
      errorListeners.put(id, errorListener);
      if (closeListener != null) {
        closeListeners.put(id, closeListener);
      }
      return () -> {
        messageListeners.remove(id);
        errorListeners.remove(id);
        closeListeners.remove(id);
      };
    }

    @Override
    public void close() {
      closed.set(true);
    }

    void emit(String payload) {
      messageListeners.values().forEach(listener -> listener.accept(payload));
    }

    boolean isClosed() {
      return closed.get();
    }

    List<String> getSentMessages() {
      return sentMessages;
    }
  }
}
