package nostr.api.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import nostr.base.RelayUri;
import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.api.NostrSpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.id.Identity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

/** Verifies default error listener emits WARN logs when subscribe path throws. */
public class NostrSpringWebSocketClientSubscribeLoggingTest {

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
  void logsWarnOnSubscribeFailureWithDefaultErrorListener() throws Exception {
    SpringWebSocketClient client = mock(SpringWebSocketClient.class);
    // Throw on subscribe to simulate transport failure
    when(client.subscribe(any(nostr.event.message.ReqMessage.class), any(), any(), any()))
        .thenThrow(new IOException("subscribe-io"));

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

    try {
      testClient.subscribe(new Filters(new KindFilter<>(nostr.base.Kind.TEXT_NOTE)), "sub-warn", s -> {});
    } catch (RuntimeException ignored) {
      // default error listener warns; the exception is rethrown by handler subscribe path
    }
    boolean found = logger.getLoggingEvents().stream()
        .anyMatch(e -> e.getLevel().toString().equals("WARN")
            && e.getMessage().contains("Subscription error on relay {} for {}")
            && e.getArguments().size() == 2
            && String.valueOf(e.getArguments().get(0)).contains("relay-1")
            && String.valueOf(e.getArguments().get(1)).contains("sub-warn"));
    assertTrue(found);
  }
}
