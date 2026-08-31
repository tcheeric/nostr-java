package nostr.mcp.integration;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import nostr.client.relay.RelayPool;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.client.testing.RelayStoresEventsWaitStrategy;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.mcp.subscription.SubscriptionLimits;
import nostr.mcp.subscription.SubscriptionRegistry;
import nostr.mcp.tool.ListSubscriptionsTool;
import nostr.mcp.tool.ReadSubscriptionTool;
import nostr.mcp.tool.SubscribeTool;
import nostr.mcp.tool.UnsubscribeTool;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Watches a real relay for events that had not happened when the subscription opened.
 *
 * <p>This is the behaviour a fake cannot demonstrate: the relay decides when to send its
 * end-of-backlog signal, events stream in on transport threads afterwards, and a note published
 * later has to find its way to a subscription opened before it existed.
 */
@Testcontainers
class SubscriptionToolsIT {

  @Container
  private static final GenericContainer<?> RELAY =
      new GenericContainer<>(DockerImageName.parse("scsibug/nostr-rs-relay:0.8.13"))
          .withExposedPorts(8080)
          .withStartupAttempts(5)
          .waitingFor(new RelayStoresEventsWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)));

  // Verifies a note published after the subscription opened is delivered to it, which is the
  // whole reason subscriptions exist rather than only queries.
  @Test
  void aNotePublishedAfterSubscribingArrives() throws Exception {
    Identity author = Identity.generateRandomIdentity();
    String content = "live " + System.nanoTime();

    try (RelayPool pool = pool();
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      String subscriptionId = subscribeTo(registry, author);

      publish(note(author, content));

      await()
          .atMost(15, TimeUnit.SECONDS)
          .until(() -> registry.require(subscriptionId).depth() > 0);
      CallToolResult read = read(registry, subscriptionId);
      assertTrue(textOf(read).contains("Received 1 event"), textOf(read));
    }
  }

  // Verifies reading drains, so a second read returns nothing rather than the same events again.
  @Test
  void readingTwiceDoesNotRepeatEvents() throws Exception {
    Identity author = Identity.generateRandomIdentity();

    try (RelayPool pool = pool();
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      String subscriptionId = subscribeTo(registry, author);
      publish(note(author, "once " + System.nanoTime()));
      await().atMost(15, TimeUnit.SECONDS).until(() -> registry.require(subscriptionId).depth() > 0);

      read(registry, subscriptionId);
      CallToolResult second = read(registry, subscriptionId);

      assertEquals(0, structuredOf(second).get("count"));
    }
  }

  // Verifies an empty read before the backlog drains is distinguishable from an empty feed, so
  // an agent does not report "no mentions" while the relay is still replaying.
  @Test
  void anEmptyReadSaysWhetherTheBacklogHasDrained() throws Exception {
    try (RelayPool pool = pool();
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      String subscriptionId = subscribeTo(registry, Identity.generateRandomIdentity());

      await()
          .atMost(15, TimeUnit.SECONDS)
          .until(() -> registry.require(subscriptionId).backlogDrained());

      assertEquals("Nothing new since the last read.", textOf(read(registry, subscriptionId)));
    }
  }

  // Verifies a full buffer tells the agent it missed events, since a silent gap would be
  // summarised as though it were the whole feed.
  @Test
  void anOverflowingBufferReportsWhatItDropped() throws Exception {
    Identity author = Identity.generateRandomIdentity();

    try (RelayPool pool = pool();
        SubscriptionRegistry registry = registryOf(pool, new SubscriptionLimits(10, 2, Duration.ofHours(1)))) {
      String subscriptionId = subscribeTo(registry, author);

      for (int index = 0; index < 5; index++) {
        publish(note(author, "flood " + index + " " + System.nanoTime()));
      }

      await()
          .atMost(20, TimeUnit.SECONDS)
          .until(() -> registry.require(subscriptionId).droppedCount() > 0);
      assertTrue(textOf(read(registry, subscriptionId)).contains("dropped"), "the gap was not reported");
    }
  }

  // Verifies listing reports what is open and how deep it is, which is how an agent resuming a
  // conversation discovers what it is already watching.
  @Test
  void listingReportsTheOpenSubscriptions() throws Exception {
    try (RelayPool pool = pool();
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      String subscriptionId = subscribeTo(registry, Identity.generateRandomIdentity());

      CallToolResult listed =
          new ListSubscriptionsTool(registry)
              .call(new CallToolRequest("nostr_list_subscriptions", Map.of()));

      assertTrue(textOf(listed).contains(subscriptionId), textOf(listed));
    }
  }

  // Verifies unsubscribing closes it, so a later read reports the id as unknown rather than
  // quietly returning nothing forever.
  @Test
  void unsubscribingClosesIt() throws Exception {
    try (RelayPool pool = pool();
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      String subscriptionId = subscribeTo(registry, Identity.generateRandomIdentity());

      new UnsubscribeTool(registry)
          .call(new CallToolRequest("nostr_unsubscribe", Map.of("subscriptionId", subscriptionId)));

      CallToolResult read = read(registry, subscriptionId);
      assertTrue(Boolean.TRUE.equals(read.isError()), textOf(read));
      assertTrue(textOf(read).startsWith("SUBSCRIPTION_UNKNOWN"), textOf(read));
    }
  }

  private String subscribeTo(SubscriptionRegistry registry, Identity author) {
    CallToolResult opened =
        new SubscribeTool(registry, Clock.systemUTC())
            .call(
                new CallToolRequest(
                    "nostr_subscribe",
                    Map.of(
                        "authors", List.of(author.getPublicKey().toHexString()),
                        "kinds", List.of(1))));
    assertFalse(Boolean.TRUE.equals(opened.isError()), textOf(opened));
    return String.valueOf(structuredOf(opened).get("subscriptionId"));
  }

  private CallToolResult read(SubscriptionRegistry registry, String subscriptionId) {
    return new ReadSubscriptionTool(registry)
        .call(
            new CallToolRequest(
                "nostr_read_subscription", Map.of("subscriptionId", subscriptionId)));
  }

  private SubscriptionRegistry registryOf(RelayPool pool, SubscriptionLimits limits) {
    return new SubscriptionRegistry(pool, limits, Clock.systemUTC(), subscriptionId -> {});
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> structuredOf(CallToolResult result) {
    return (Map<String, Object>) result.structuredContent();
  }

  private GenericEvent note(Identity author, String content) {
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(author.getPublicKey())
            .kind(1)
            .content(content)
            .createdAt(System.currentTimeMillis() / 1000)
            .build();
    event.update();
    author.sign(event);
    return event;
  }

  private void publish(GenericEvent event) throws Exception {
    try (RelayPool pool = pool()) {
      pool.publish(event);
    }
  }

  private RelayPool pool() {
    return new RelayPool(List.of(relayUri()), SubscriptionToolsIT::connect);
  }

  private static nostr.client.relay.RelayConnection connect(String relayUri) throws IOException {
    try {
      return new NostrRelayClient(relayUri, 30_000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(e);
    } catch (ExecutionException e) {
      throw new IOException(e.getCause());
    }
  }

  private String textOf(CallToolResult result) {
    return result.content().stream()
        .filter(TextContent.class::isInstance)
        .map(TextContent.class::cast)
        .map(TextContent::text)
        .findFirst()
        .orElse("");
  }

  private static String relayUri() {
    return "ws://" + RELAY.getHost() + ":" + RELAY.getMappedPort(8080);
  }
}
