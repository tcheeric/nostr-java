package nostr.client.relay;

import nostr.base.PublicKey;
import nostr.client.springwebsocket.ConnectionState;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the pool respects each relay's one-request-in-flight limit without giving up
 * concurrency across relays, and that a relay which recovers rejoins on its own.
 */
class RelayPoolConcurrencyTest {

  private static final String FIRST_RELAY = "wss://relay.one";
  private static final String SECOND_RELAY = "wss://relay.two";
  private static final int CONCURRENT_PUBLISHES = 8;
  private static final int RACE_ROUNDS = 60;
  private static final int RETRIES_PER_ROUND = 20;

  // Verifies many threads publishing to the same relay at once all succeed: a connection accepts
  // one request at a time, so the pool must queue them rather than let them collide.
  @Test
  void concurrentPublishesToOneRelayAreQueuedRatherThanRejected() throws Exception {
    FakeRelay relay = FakeRelay.accepting(FIRST_RELAY);

    try (RelayPool pool = new RelayPool(List.of(FIRST_RELAY), relayUri -> relay);
        ExecutorService callers = Executors.newFixedThreadPool(CONCURRENT_PUBLISHES)) {

      CountDownLatch startTogether = new CountDownLatch(1);
      List<Future<PublishResult>> published = new ArrayList<>();
      for (int publish = 0; publish < CONCURRENT_PUBLISHES; publish++) {
        published.add(
            callers.submit(
                () -> {
                  startTogether.await();
                  return pool.publish(signedEvent());
                }));
      }
      startTogether.countDown();

      for (Future<PublishResult> outcome : published) {
        assertEquals(List.of(FIRST_RELAY), outcome.get(10, TimeUnit.SECONDS).getAcceptingRelays());
      }
      assertEquals(
          1, relay.getPeakConcurrentSends(), "the relay saw overlapping sends, so it was not queued");
    }
  }

  // Verifies queuing per relay does not serialise the whole pool: two relays must still be
  // published to at the same time, which only holds if each relay has its own queue.
  @Test
  void publishesToDifferentRelaysStillOverlap() throws Exception {
    AtomicInteger sendsInFlight = new AtomicInteger();
    AtomicInteger peakInFlight = new AtomicInteger();
    CountDownLatch bothArrived = new CountDownLatch(2);
    RelayConnectionFactory factory =
        relayUri ->
            FakeRelay.acceptingWhile(
                relayUri,
                () -> {
                  peakInFlight.accumulateAndGet(sendsInFlight.incrementAndGet(), Math::max);
                  bothArrived.countDown();
                  awaitQuietly(bothArrived);
                  sendsInFlight.decrementAndGet();
                });

    try (RelayPool pool = new RelayPool(List.of(FIRST_RELAY, SECOND_RELAY), factory)) {
      PublishResult result = pool.publish(signedEvent());

      assertEquals(List.of(FIRST_RELAY, SECOND_RELAY), result.getAcceptingRelays());
      assertEquals(2, peakInFlight.get(), "relays were published to one after another");
    }
  }

  // Verifies the pool reports each relay's connection state, so an operator can tell which
  // relays are actually carrying traffic.
  @Test
  void perRelayConnectionStateIsObservable() throws Exception {
    FakeRelay healthy = FakeRelay.accepting(FIRST_RELAY);
    FakeRelay dropped = FakeRelay.accepting(SECOND_RELAY);

    try (RelayPool pool =
        new RelayPool(
            List.of(FIRST_RELAY, SECOND_RELAY),
            relayUri -> FIRST_RELAY.equals(relayUri) ? healthy : dropped)) {

      assertEquals(ConnectionState.CONNECTED, pool.getConnectionState(FIRST_RELAY).orElseThrow());

      dropped.dropConnection();

      assertEquals(ConnectionState.CLOSED, pool.getConnectionState(SECOND_RELAY).orElseThrow());
      assertEquals(List.of(FIRST_RELAY), pool.getConnectedRelays());
      assertTrue(pool.getConnectionState("wss://relay.never-configured").isEmpty());
    }
  }

  // Verifies a relay that was unreachable at startup rejoins once it recovers, so a restart is
  // not needed to pick it back up.
  @Test
  void aRelayThatRecoversRejoinsWithoutARestart() throws Exception {
    AtomicInteger connectionAttempts = new AtomicInteger();
    RelayConnectionFactory intermittent =
        relayUri -> {
          if (SECOND_RELAY.equals(relayUri) && connectionAttempts.incrementAndGet() == 1) {
            throw new IOException("connection refused");
          }
          return FakeRelay.accepting(relayUri);
        };

    try (RelayPool pool = new RelayPool(List.of(FIRST_RELAY, SECOND_RELAY), intermittent)) {
      assertEquals(List.of(SECOND_RELAY), pool.getUnreachableRelays());
      assertFalse(pool.publish(signedEvent()).getAcceptingRelays().contains(SECOND_RELAY));

      pool.retryUnreachableRelays();

      assertEquals(List.of(), pool.getUnreachableRelays());
      assertEquals(
          List.of(FIRST_RELAY, SECOND_RELAY), pool.publish(signedEvent()).getAcceptingRelays());
    }
  }

  // Verifies a relay that is still down after a retry stays marked down rather than being
  // wrongly returned to service.
  @Test
  void aRelayThatIsStillDownStaysMarkedDown() throws Exception {
    RelayConnectionFactory alwaysFailing =
        relayUri -> {
          if (SECOND_RELAY.equals(relayUri)) {
            throw new IOException("connection refused");
          }
          return FakeRelay.accepting(relayUri);
        };

    try (RelayPool pool = new RelayPool(List.of(FIRST_RELAY, SECOND_RELAY), alwaysFailing)) {
      pool.retryUnreachableRelays();

      assertEquals(List.of(SECOND_RELAY), pool.getUnreachableRelays());
      assertEquals(List.of(FIRST_RELAY), pool.getConnectedRelays());
    }
  }

  // Verifies retrying downed relays while publishes are in flight does not corrupt the pool:
  // membership changes from another thread must not disturb a publish already iterating it.
  @Test
  void retryingDownedRelaysDuringPublishesIsSafe() throws Exception {
    AtomicReference<Throwable> failure = new AtomicReference<>();
    RelayConnectionFactory intermittent =
        relayUri -> {
          if (relayUri.contains("flaky") && ThreadLocalRandom.current().nextBoolean()) {
            throw new IOException("connection refused");
          }
          return FakeRelay.accepting(relayUri);
        };

    for (int round = 0; round < RACE_ROUNDS && failure.get() == null; round++) {
      try (RelayPool pool =
              new RelayPool(
                  List.of(FIRST_RELAY, "wss://relay.flaky-one", "wss://relay.flaky-two"),
                  intermittent);
          ExecutorService callers = Executors.newFixedThreadPool(3)) {

        CountDownLatch startTogether = new CountDownLatch(1);
        for (int publisher = 0; publisher < 2; publisher++) {
          callers.submit(() -> publishQuietly(pool, startTogether, failure));
        }
        callers.submit(() -> retryQuietly(pool, startTogether, failure));
        startTogether.countDown();
        callers.shutdown();
        assertTrue(callers.awaitTermination(10, TimeUnit.SECONDS));
      }
    }

    assertNull(failure.get(), "publishing while relays rejoined corrupted the pool");
  }

  private void publishQuietly(
      RelayPool pool, CountDownLatch startTogether, AtomicReference<Throwable> failure) {
    try {
      startTogether.await();
      pool.publish(signedEvent());
    } catch (NoRelayAcceptedException expected) {
      // Every relay may legitimately be down in a given round.
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Throwable e) {
      failure.compareAndSet(null, e);
    }
  }

  private void retryQuietly(
      RelayPool pool, CountDownLatch startTogether, AtomicReference<Throwable> failure) {
    try {
      startTogether.await();
      for (int attempt = 0; attempt < RETRIES_PER_ROUND; attempt++) {
        pool.retryUnreachableRelays();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Throwable e) {
      failure.compareAndSet(null, e);
    }
  }

  // Verifies the pool retries downed relays on its own schedule, so an operator does not have to
  // remember to poll for recovery.
  @Test
  void downedRelaysAreRetriedInTheBackground() throws Exception {
    AtomicInteger connectionAttempts = new AtomicInteger();
    RelayConnectionFactory recoveringOnSecondAttempt =
        relayUri -> {
          if (SECOND_RELAY.equals(relayUri) && connectionAttempts.incrementAndGet() == 1) {
            throw new IOException("connection refused");
          }
          return FakeRelay.accepting(relayUri);
        };

    try (RelayPool pool =
        new RelayPool(
            List.of(FIRST_RELAY, SECOND_RELAY),
            recoveringOnSecondAttempt,
            RelayPool.DEFAULT_PUBLISH_TIMEOUT,
            Duration.ofMillis(50))) {

      assertEquals(List.of(SECOND_RELAY), pool.getUnreachableRelays());

      await().atMost(5, TimeUnit.SECONDS)
          .until(() -> pool.getConnectedRelays().contains(SECOND_RELAY));

      assertEquals(List.of(), pool.getUnreachableRelays());
    }
  }

  // Verifies a relay that drops after connecting is reconnected too, not just one that failed at
  // startup, so a long-running pool does not quietly shrink.
  @Test
  void aRelayThatDropsAfterConnectingIsReconnected() throws Exception {
    FakeRelay initial = FakeRelay.accepting(SECOND_RELAY);
    AtomicInteger connectionAttempts = new AtomicInteger();
    RelayConnectionFactory factory =
        relayUri -> {
          if (!SECOND_RELAY.equals(relayUri)) {
            return FakeRelay.accepting(relayUri);
          }
          return connectionAttempts.incrementAndGet() == 1 ? initial : FakeRelay.accepting(relayUri);
        };

    try (RelayPool pool =
        new RelayPool(
            List.of(FIRST_RELAY, SECOND_RELAY),
            factory,
            RelayPool.DEFAULT_PUBLISH_TIMEOUT,
            Duration.ofMillis(50))) {

      assertEquals(List.of(FIRST_RELAY, SECOND_RELAY), pool.getConnectedRelays());

      initial.dropConnection();

      await().atMost(5, TimeUnit.SECONDS)
          .until(() -> pool.getConnectionState(SECOND_RELAY).orElseThrow() == ConnectionState.CONNECTED);
      assertEquals(
          List.of(FIRST_RELAY, SECOND_RELAY), pool.publish(signedEvent()).getAcceptingRelays());
    }
  }

  private static void awaitQuietly(CountDownLatch latch) {
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private GenericEvent signedEvent() {
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
