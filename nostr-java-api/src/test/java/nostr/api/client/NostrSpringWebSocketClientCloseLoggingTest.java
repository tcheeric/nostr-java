package nostr.api.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import lombok.NonNull;
import nostr.api.NostrSpringWebSocketClient;
import nostr.base.RelayUri;
import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.id.Identity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

/** Verifies default error listener logs WARN lines when close path encounters exceptions. */
public class NostrSpringWebSocketClientCloseLoggingTest {

  private final TestLogger logger = TestLoggerFactory.getTestLogger(NostrSpringWebSocketClient.class);

  static class TestClient extends NostrSpringWebSocketClient {
    private final WebSocketClientHandler handler;
    TestClient(Identity sender, WebSocketClientHandler handler) { super(sender); this.handler = handler; }

    @Override
    protected WebSocketClientHandler newWebSocketClientHandler(String relayName, RelayUri relayUri)
        throws ExecutionException, InterruptedException {
      return handler;
    }
  }

  @AfterEach
  void cleanup() { TestLoggerFactory.clear(); }

  @Test
  void logsWarnsOnCloseErrors() throws Exception {
    // Prepare a handler with mocked Spring client throwing on close
    SpringWebSocketClient client = mock(SpringWebSocketClient.class);
    AutoCloseable delegate = mock(AutoCloseable.class);
    AutoCloseable closeFrame = mock(AutoCloseable.class);
    when(client.subscribe(any(nostr.event.message.ReqMessage.class), any(), any(), any())).thenReturn(delegate);
    when(client.subscribe(any(nostr.event.message.CloseMessage.class), any(), any(), any())).thenReturn(closeFrame);
    doThrow(new IOException("cf")).when(closeFrame).close();
    doThrow(new RuntimeException("del")).when(delegate).close();

    WebSocketClientFactory factory = mock(WebSocketClientFactory.class);
    Function<SubscriptionId, SpringWebSocketClient> reqFactory = k -> client;
    WebSocketClientHandler handler =
        new WebSocketClientHandler(
            "relay-1",
            new RelayUri("wss://relay1"),
            client,
            new HashMap<>(),
            reqFactory,
            factory);

    Identity sender = Identity.generateRandomIdentity();
    TestClient testClient = new TestClient(sender, handler);
    testClient.setRelays(Map.of("r1", "wss://relay1"));

    AutoCloseable h = testClient.subscribe(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-close-log", s -> {});
    try {
      try {
        h.close();
      } catch (IOException ignored) {}
      boolean found = logger.getLoggingEvents().stream()
          .anyMatch(e -> e.getLevel().toString().equals("WARN")
              && e.getMessage().contains("Subscription error for {} on relays {}")
              && e.getArguments().size() == 2
              && String.valueOf(e.getArguments().get(0)).contains("sub-close-log")
              && String.valueOf(e.getArguments().get(1)).contains("relay"));
      assertTrue(found);
    } finally {
      try { h.close(); } catch (Exception ignored) {}
    }
  }
}
