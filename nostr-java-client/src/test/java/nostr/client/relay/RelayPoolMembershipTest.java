package nostr.client.relay;

import nostr.base.PublicKey;
import nostr.client.springwebsocket.ConnectionState;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies relays can join and leave a running pool, and that a relay borrowed for one operation
 * is released without cutting off another caller still using it.
 */
class RelayPoolMembershipTest {

  private static final String CONFIGURED_RELAY = "wss://relay.configured";
  private static final String BORROWED_RELAY = "wss://relay.borrowed";
  private static final List<EventFilter> TEXT_NOTES =
      List.of(EventFilter.builder().kind(1).build());

  private final Map<String, FakeRelay> openedRelays = new ConcurrentHashMap<>();

  // Verifies a relay added at runtime immediately takes part in publishing, so a relay set can
  // follow user preferences without restarting the application.
  @Test
  void aRelayAddedAtRuntimeParticipatesImmediately() throws Exception {
    try (RelayPool pool = poolOf(CONFIGURED_RELAY)) {
      assertEquals(List.of(CONFIGURED_RELAY), pool.publish(event()).getAcceptingRelays());

      assertTrue(pool.addRelay(BORROWED_RELAY));

      assertEquals(
          List.of(CONFIGURED_RELAY, BORROWED_RELAY), pool.publish(event()).getAcceptingRelays());
    }
  }

  // Verifies removing a relay stops it being published to and closes its connection.
  @Test
  void aRemovedRelayIsClosedAndNoLongerPublishedTo() throws Exception {
    try (RelayPool pool = poolOf(CONFIGURED_RELAY, BORROWED_RELAY)) {
      assertTrue(pool.removeRelay(BORROWED_RELAY));

      assertEquals(List.of(CONFIGURED_RELAY), pool.publish(event()).getAcceptingRelays());
      assertEquals(List.of(CONFIGURED_RELAY), pool.getRelays());
      assertEquals(
          ConnectionState.CLOSED,
          openedRelays.get(BORROWED_RELAY).getConnectionState());
    }
  }

  // Verifies adding a relay already in the pool does not open a second connection to it, since
  // relays penalise clients that hold many sockets.
  @Test
  void addingAnExistingRelayDoesNotOpenASecondConnection() throws Exception {
    AtomicInteger connectionsOpened = new AtomicInteger();
    RelayConnectionFactory counting =
        relayUri -> {
          connectionsOpened.incrementAndGet();
          return FakeRelay.accepting(relayUri);
        };

    try (RelayPool pool = new RelayPool(List.of(CONFIGURED_RELAY), counting)) {
      pool.addRelay(CONFIGURED_RELAY);
      pool.addRelay(CONFIGURED_RELAY);

      assertEquals(1, connectionsOpened.get());
      assertEquals(List.of(CONFIGURED_RELAY), pool.getRelays());
    }
  }

  // Verifies a relay borrowed for one operation is closed when released, so connections opened
  // for a single delivery do not accumulate over a long-running process.
  @Test
  void aBorrowedRelayIsClosedWhenReleased() throws Exception {
    try (RelayPool pool = poolOf(CONFIGURED_RELAY)) {
      pool.addRelay(BORROWED_RELAY);

      assertTrue(pool.releaseRelay(BORROWED_RELAY));

      assertEquals(List.of(CONFIGURED_RELAY), pool.getRelays());
      assertEquals(
          ConnectionState.CLOSED,
          openedRelays.get(BORROWED_RELAY).getConnectionState());
    }
  }

  // Verifies a relay borrowed twice survives the first release, so two overlapping deliveries to
  // the same recipient do not cut each other off.
  @Test
  void aRelayBorrowedTwiceSurvivesTheFirstRelease() throws Exception {
    try (RelayPool pool = poolOf(CONFIGURED_RELAY)) {
      pool.addRelay(BORROWED_RELAY);
      pool.addRelay(BORROWED_RELAY);

      assertFalse(pool.releaseRelay(BORROWED_RELAY), "the relay closed while still in use");
      assertEquals(
          List.of(CONFIGURED_RELAY, BORROWED_RELAY), pool.publish(event()).getAcceptingRelays());

      assertTrue(pool.releaseRelay(BORROWED_RELAY));
      assertEquals(List.of(CONFIGURED_RELAY), pool.getRelays());
    }
  }

  // Verifies a relay joining a running pool is subscribed to existing subscriptions, so it
  // starts contributing events rather than sitting idle.
  @Test
  void aRelayAddedDuringASubscriptionStartsContributing() throws Exception {
    try (RelayPool pool = poolOf(CONFIGURED_RELAY);
        RelaySubscription subscription = pool.subscribe(TEXT_NOTES, receivedEvent -> {})) {

      pool.addRelay(BORROWED_RELAY);

      assertTrue(subscription.getSubscribedRelays().contains(BORROWED_RELAY));
      assertEquals(
          List.of(subscription.getSubscriptionId()),
          openedRelays.get(BORROWED_RELAY).getSentSubscriptionIds());
    }
  }

  // Verifies removing a relay that was never a member reports that nothing was removed.
  @Test
  void removingAnAbsentRelayReportsNothingWasRemoved() throws Exception {
    try (RelayPool pool = poolOf(CONFIGURED_RELAY)) {
      assertFalse(pool.removeRelay("wss://relay.never-joined"));
    }
  }

  private RelayPool poolOf(String... relayUris) {
    return new RelayPool(
        List.of(relayUris),
        relayUri -> openedRelays.computeIfAbsent(relayUri, FakeRelay::accepting));
  }

  private GenericEvent event() {
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(new PublicKey("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
            .kind(1)
            .content("hello")
            .build();
    event.update();
    return event;
  }
}
