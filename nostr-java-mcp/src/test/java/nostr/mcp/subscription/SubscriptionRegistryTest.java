package nostr.mcp.subscription;

import nostr.client.relay.FakeRelay;
import nostr.client.relay.RelayPool;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.mcp.tool.ToolException;
import nostr.mcp.tool.ToolFailure;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies subscriptions are handed out safely and taken back when nobody is using them. */
class SubscriptionRegistryTest {

  private static final String RELAY = "wss://relay.one";

  // Verifies an opened subscription is named and reachable, which is what lets an agent come
  // back to it in a later turn.
  @Test
  void anOpenedSubscriptionCanBeFoundAgain() {
    FakeRelay relay = FakeRelay.accepting(RELAY);
    try (RelayPool pool = poolOf(relay);
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {

      LiveSubscription opened = registry.open(anyNote());

      assertEquals(opened.id(), registry.require(opened.id()).id());
      assertEquals(List.of(opened.id()), registry.list().stream().map(LiveSubscription::id).toList());
    }
  }

  // Verifies events arriving after the call returns are buffered, which is the entire reason
  // subscriptions exist rather than only queries.
  @Test
  void eventsArrivingLaterAreBuffered() {
    FakeRelay relay = FakeRelay.accepting(RELAY);
    try (RelayPool pool = poolOf(relay);
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      LiveSubscription opened = registry.open(anyNote());

      relay.emitEvent(relaySubscriptionId(relay), note("arrived later"));

      await().atMost(2, TimeUnit.SECONDS).until(() -> opened.depth() == 1);
      assertEquals("arrived later", opened.drain().getFirst().getContent());
    }
  }

  // Verifies a subscription does not claim its backlog is drained before the relay says so, so
  // an agent can tell "nothing matched yet" from "still replaying".
  @Test
  void theBacklogIsNotClaimedDrainedUntilTheRelaySaysSo() {
    FakeRelay relay = FakeRelay.accepting(RELAY);
    try (RelayPool pool = poolOf(relay);
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      LiveSubscription opened = registry.open(anyNote());

      assertFalse(opened.backlogDrained(), "the backlog was claimed drained before any signal");

      relay.emitEndOfStoredEvents(relaySubscriptionId(relay));
      await().atMost(2, TimeUnit.SECONDS).until(opened::backlogDrained);
    }
  }

  // Verifies the cap turns a runaway agent into an error it can see, rather than a server that
  // slowly stops responding.
  @Test
  void theSubscriptionCapIsEnforced() {
    try (RelayPool pool = poolOf(FakeRelay.accepting(RELAY));
        SubscriptionRegistry registry =
            registryOf(pool, new SubscriptionLimits(2, 10, Duration.ofHours(1)))) {
      registry.open(anyNote());
      registry.open(anyNote());

      ToolException refused = assertThrows(ToolException.class, () -> registry.open(anyNote()));

      assertEquals(ToolFailure.SUBSCRIPTION_LIMIT_REACHED, refused.getFailure());
      assertTrue(refused.getMessage().contains("nostr_unsubscribe"), refused.getMessage());
    }
  }

  // Verifies closing frees a slot, so an agent that tidies up can carry on working.
  @Test
  void closingFreesASlot() {
    try (RelayPool pool = poolOf(FakeRelay.accepting(RELAY));
        SubscriptionRegistry registry =
            registryOf(pool, new SubscriptionLimits(1, 10, Duration.ofHours(1)))) {
      LiveSubscription first = registry.open(anyNote());
      assertThrows(ToolException.class, () -> registry.open(anyNote()), "the cap was not enforced");

      registry.close(first.id());

      LiveSubscription second = registry.open(anyNote());
      assertEquals(List.of(second.id()), registry.list().stream().map(LiveSubscription::id).toList());
    }
  }

  // Verifies naming a subscription that does not exist explains itself and lists what is open,
  // since an agent resuming a conversation may be holding a reaped id.
  @Test
  void anUnknownSubscriptionIsExplained() {
    try (RelayPool pool = poolOf(FakeRelay.accepting(RELAY));
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {

      ToolException unknown = assertThrows(ToolException.class, () -> registry.require("sub-99"));

      assertEquals(ToolFailure.SUBSCRIPTION_UNKNOWN, unknown.getFailure());
      assertTrue(unknown.getMessage().contains("idle"), unknown.getMessage());
    }
  }

  // Verifies an abandoned subscription is reaped, since an agent's session can end without this
  // server ever being told and its relay traffic would otherwise run forever.
  @Test
  void anIdleSubscriptionIsReaped() {
    try (RelayPool pool = poolOf(FakeRelay.accepting(RELAY));
        SubscriptionRegistry registry =
            registryOf(pool, new SubscriptionLimits(10, 10, Duration.ofMillis(200)))) {
      LiveSubscription opened = registry.open(anyNote());

      await()
          .atMost(5, TimeUnit.SECONDS)
          .until(() -> registry.find(opened.id()).isEmpty());
    }
  }

  // Verifies reading keeps a subscription alive, so one an agent is actually using is not reaped
  // out from under it.
  @Test
  void readingKeepsASubscriptionAlive() throws Exception {
    try (RelayPool pool = poolOf(FakeRelay.accepting(RELAY));
        SubscriptionRegistry registry =
            registryOf(pool, new SubscriptionLimits(10, 10, Duration.ofMillis(400)))) {
      LiveSubscription opened = registry.open(anyNote());

      for (int poll = 0; poll < 6; poll++) {
        Thread.sleep(100);
        opened.drain();
      }

      assertTrue(registry.find(opened.id()).isPresent(), "an actively read subscription was reaped");
    }
  }

  // Verifies a relay dropping out is recorded, so degraded coverage is visible rather than
  // silently narrowing what the agent sees.
  @Test
  void aRelayDroppingOutIsRecorded() {
    FakeRelay relay = FakeRelay.accepting(RELAY);
    try (RelayPool pool = poolOf(relay);
        SubscriptionRegistry registry = registryOf(pool, SubscriptionLimits.defaults())) {
      LiveSubscription opened = registry.open(anyNote());

      relay.dropConnection();

      await().atMost(2, TimeUnit.SECONDS).until(() -> !opened.failures().isEmpty());
      assertTrue(opened.failures().containsKey(RELAY));
    }
  }

  // Verifies arriving events notify the host, which is what lets a subscription push rather than
  // waiting for the model to remember to poll.
  @Test
  void arrivingEventsNotifyTheHost() {
    List<String> notified = new CopyOnWriteArrayList<>();
    FakeRelay relay = FakeRelay.accepting(RELAY);
    try (RelayPool pool = poolOf(relay);
        SubscriptionRegistry registry =
            new SubscriptionRegistry(
                pool, SubscriptionLimits.defaults(), Clock.systemUTC(), notified::add)) {
      LiveSubscription opened = registry.open(anyNote());

      relay.emitEvent(relaySubscriptionId(relay), note("ping"));

      await().atMost(2, TimeUnit.SECONDS).until(() -> notified.contains(opened.id()));
    }
  }

  private String relaySubscriptionId(FakeRelay relay) {
    return relay.getSentSubscriptionIds().getFirst();
  }

  private SubscriptionRegistry registryOf(RelayPool pool, SubscriptionLimits limits) {
    return new SubscriptionRegistry(pool, limits, Clock.systemUTC(), subscriptionId -> {});
  }

  private RelayPool poolOf(FakeRelay relay) {
    return new RelayPool(List.of(RELAY), relayUri -> relay);
  }

  private EventFilter anyNote() {
    return EventFilter.builder().kind(1).build();
  }

  private GenericEvent note(String content) {
    Identity author = Identity.generateRandomIdentity();
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
}
