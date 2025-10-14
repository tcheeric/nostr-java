package nostr.api.client;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import nostr.api.NostrSpringWebSocketClient;
import nostr.api.integration.support.FakeWebSocketClientFactory;
import nostr.api.service.impl.DefaultNoteService;
import nostr.base.Kind;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import nostr.id.Identity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies default error listener path emits a WARN log entry. */
public class NostrSpringWebSocketClientLoggingTest {

  private final TestLogger logger = TestLoggerFactory.getTestLogger(NostrSpringWebSocketClient.class);

  @AfterEach
  void cleanup() { TestLoggerFactory.clear(); }

  @Test
  void defaultErrorListenerEmitsWarnLog() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client =
        new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);

    client.setRelays(Map.of("relay", "wss://relay.example.com"));
    AutoCloseable handle = client.subscribe(new Filters(new KindFilter<>(Kind.TEXT_NOTE)), "sub-log", s -> {});
    try {
      factory.get("wss://relay.example.com").emitError(new RuntimeException("log-me"));
      boolean found = logger.getLoggingEvents().stream()
          .anyMatch(e -> e.getLevel().toString().equals("WARN")
              && e.getMessage().contains("Subscription error for {} on relays {}")
              && e.getArguments().size() == 2
              && String.valueOf(e.getArguments().get(0)).contains("sub-log")
              && String.valueOf(e.getArguments().get(1)).contains("relay"));
      assertTrue(found);
    } finally {
      handle.close();
    }
  }
}
