package nostr.examples;

import java.time.Duration;
import java.util.Map;
import nostr.api.NostrSpringWebSocketClient;
import nostr.base.Kind;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;

/**
 * Example showing how to open a non-blocking subscription using {@link NostrSpringWebSocketClient}
 * and close it after a fixed duration.
 */
public class SpringSubscriptionExample {

  private static final Map<String, String> RELAYS = Map.of("local", "ws://localhost:5555");
  private static final Duration LISTEN_DURATION = Duration.ofSeconds(30);

  public static void main(String[] args) throws Exception {
    NostrSpringWebSocketClient client = new NostrSpringWebSocketClient();
    client.setRelays(RELAYS);

    Filters filters = new Filters(new KindFilter<>(Kind.TEXT_NOTE));

    AutoCloseable subscription =
        client.subscribe(
            filters,
            "example-subscription",
            message -> System.out.printf("Received from relay: %s%n", message),
            error ->
                System.err.printf(
                    "Subscription error for %s: %s%n", RELAYS.keySet(), error.getMessage()));

    try {
      System.out.printf(
          "Listening for %d seconds. Publish events to %s to see them here.%n",
          LISTEN_DURATION.toSeconds(), RELAYS.values());
      Thread.sleep(LISTEN_DURATION.toMillis());
    } finally {
      try {
        subscription.close();
      } finally {
        client.close();
      }
    }
  }
}
