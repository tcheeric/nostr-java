package nostr.client.relay;

import nostr.base.PublicKey;
import nostr.client.springwebsocket.ConnectionState;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CyclicBarrier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the pool publishes across relays and reports what each one did, including when they
 * disagree.
 */
class RelayPoolTest {

  private static final String ACCEPTING_RELAY = "wss://relay.accepts";
  private static final String SECOND_ACCEPTING_RELAY = "wss://relay.also-accepts";
  private static final String THIRD_ACCEPTING_RELAY = "wss://relay.accepts-too";
  private static final String REJECTING_RELAY = "wss://relay.rejects";
  private static final String SILENT_RELAY = "wss://relay.silent";
  private static final String UNREACHABLE_RELAY = "wss://relay.down";
  private static final String BAN_REASON = "blocked: pubkey banned";
  private static final long UNINTERRUPTIBLE_SEND_MILLIS = 3_000L;

  // Verifies a publish to five disagreeing relays reports three acceptances, the rejecting
  // relay's verbatim reason, and the silent relay as timed out, rather than one overall verdict.
  @Test
  void publishReportsWhatEachRelayDidWhenTheyDisagree() throws Exception {
    try (RelayPool pool = poolOf(
        FakeRelay.accepting(ACCEPTING_RELAY),
        FakeRelay.accepting(SECOND_ACCEPTING_RELAY),
        FakeRelay.accepting(THIRD_ACCEPTING_RELAY),
        FakeRelay.rejecting(REJECTING_RELAY, BAN_REASON),
        FakeRelay.silent(SILENT_RELAY))) {

      PublishResult result = pool.publish(signedEvent());

      assertEquals(
          List.of(ACCEPTING_RELAY, SECOND_ACCEPTING_RELAY, THIRD_ACCEPTING_RELAY),
          result.getAcceptingRelays());
      assertEquals(
          RelayPublishOutcome.Status.REJECTED,
          result.findOutcome(REJECTING_RELAY).orElseThrow().status());
      assertEquals(
          BAN_REASON, result.findOutcome(REJECTING_RELAY).orElseThrow().findReason().orElseThrow());
      assertEquals(
          RelayPublishOutcome.Status.TIMED_OUT,
          result.findOutcome(SILENT_RELAY).orElseThrow().status());
      assertTrue(result.isAccepted());
      assertFalse(result.isAcceptedByAllRelays());
    }
  }

  // Verifies a publish that no relay accepted throws rather than returning, so an event that
  // reached nobody cannot be mistaken for a published one.
  @Test
  void publishThrowsWhenNoRelayAcceptedTheEvent() throws Exception {
    try (RelayPool pool = poolOf(
        FakeRelay.rejecting(REJECTING_RELAY, BAN_REASON), FakeRelay.silent(SILENT_RELAY))) {

      NoRelayAcceptedException thrown =
          assertThrows(NoRelayAcceptedException.class, () -> pool.publish(signedEvent()));

      assertEquals(2, thrown.getPublishResult().getFailures().size());
      assertTrue(thrown.getMessage().contains(BAN_REASON), "the relay's reason should survive");
    }
  }

  // Verifies a single acceptance is enough for the publish to return, however many relays failed.
  @Test
  void publishReturnsWhenOneRelayAcceptedAmongFailures() throws Exception {
    try (RelayPool pool = poolOf(
        FakeRelay.rejecting(REJECTING_RELAY, BAN_REASON),
        FakeRelay.accepting(ACCEPTING_RELAY),
        FakeRelay.silent(SILENT_RELAY))) {

      PublishResult result = pool.publish(signedEvent());

      assertEquals(List.of(ACCEPTING_RELAY), result.getAcceptingRelays());
      assertEquals(2, result.getFailures().size());
    }
  }

  // Verifies the pool still starts and publishes when a configured relay cannot be reached,
  // so one dead relay cannot stop an application booting.
  @Test
  void poolStartsAndPublishesDespiteAnUnreachableRelay() throws Exception {
    RelayConnectionFactory factory =
        relayUri -> {
          if (UNREACHABLE_RELAY.equals(relayUri)) {
            throw new IOException("connection refused");
          }
          return FakeRelay.accepting(relayUri);
        };

    try (RelayPool pool =
        new RelayPool(List.of(ACCEPTING_RELAY, UNREACHABLE_RELAY), factory)) {

      assertEquals(List.of(ACCEPTING_RELAY), pool.getConnectedRelays());
      assertEquals(List.of(UNREACHABLE_RELAY), pool.getUnreachableRelays());

      PublishResult result = pool.publish(signedEvent());

      assertEquals(List.of(ACCEPTING_RELAY), result.getAcceptingRelays());
      assertEquals(
          RelayPublishOutcome.Status.UNREACHABLE,
          result.findOutcome(UNREACHABLE_RELAY).orElseThrow().status());
    }
  }

  // Verifies every configured relay receives the event, so fan-out reaches the whole pool.
  @Test
  void everyConnectedRelayReceivesTheEvent() throws Exception {
    FakeRelay first = FakeRelay.accepting(ACCEPTING_RELAY);
    FakeRelay second = FakeRelay.accepting(SECOND_ACCEPTING_RELAY);

    try (RelayPool pool = poolOf(first, second)) {
      pool.publish(signedEvent());
    }

    assertEquals(1, first.getSentMessages().size());
    assertEquals(1, second.getSentMessages().size());
  }

  // Verifies a relay that never answers is bounded by the pool's timeout rather than hanging
  // the publish, and is reported as timed out.
  @Test
  void aSilentRelayIsBoundedByTheConfiguredTimeout() throws Exception {
    FakeRelay stalling = FakeRelay.stalling(SILENT_RELAY);
    RelayConnectionFactory factory =
        relayUri ->
            SILENT_RELAY.equals(relayUri) ? stalling : FakeRelay.accepting(relayUri);

    try (RelayPool pool =
        new RelayPool(
            List.of(ACCEPTING_RELAY, SILENT_RELAY), factory, Duration.ofMillis(200))) {

      PublishResult result = pool.publish(signedEvent());

      assertEquals(List.of(ACCEPTING_RELAY), result.getAcceptingRelays());
      assertEquals(
          RelayPublishOutcome.Status.TIMED_OUT,
          result.findOutcome(SILENT_RELAY).orElseThrow().status());
    } finally {
      stalling.release();
    }
  }

  // Verifies relays are published to concurrently rather than one after another: three relays
  // that each block until all three have been reached can only complete if the sends overlap.
  @Test
  void relaysArePublishedToConcurrently() throws Exception {
    CyclicBarrier allThreeReached = new CyclicBarrier(3);
    List<String> relayUris =
        List.of(ACCEPTING_RELAY, SECOND_ACCEPTING_RELAY, THIRD_ACCEPTING_RELAY);
    RelayConnectionFactory factory =
        relayUri -> FakeRelay.acceptingAfter(relayUri, allThreeReached);

    try (RelayPool pool = new RelayPool(relayUris, factory, Duration.ofSeconds(5))) {
      PublishResult result = pool.publish(signedEvent());

      assertEquals(relayUris, result.getAcceptingRelays());
    }
  }

  // Verifies the pool's timeout still bounds a publish when a relay ignores interruption, so one
  // stuck transport cannot make the call outlast the timeout that exists to bound it.
  @Test
  void publishHonoursItsTimeoutEvenWhenARelayIgnoresInterruption() throws Exception {
    RelayConnectionFactory factory =
        relayUri ->
            SILENT_RELAY.equals(relayUri)
                ? new UninterruptibleRelay(relayUri)
                : FakeRelay.accepting(relayUri);

    long startedAt = System.currentTimeMillis();
    try (RelayPool pool =
        new RelayPool(
            List.of(ACCEPTING_RELAY, SILENT_RELAY), factory, Duration.ofMillis(300))) {

      PublishResult result = pool.publish(signedEvent());
      long elapsed = System.currentTimeMillis() - startedAt;

      assertEquals(List.of(ACCEPTING_RELAY), result.getAcceptingRelays());
      assertTrue(
          elapsed < UNINTERRUPTIBLE_SEND_MILLIS,
          "publish took " + elapsed + "ms, so the timeout did not bound the stuck relay");
    }
  }

  /** A relay whose send ignores interruption, as a transport stuck in blocking I/O would. */
  private static final class UninterruptibleRelay implements RelayConnection {
    private final String relayUri;

    private UninterruptibleRelay(String relayUri) {
      this.relayUri = relayUri;
    }

    @Override
    public String getRelayUri() {
      return relayUri;
    }

    @Override
    public ConnectionState getConnectionState() {
      return ConnectionState.CONNECTED;
    }

    @Override
    public <T extends BaseMessage> List<String> send(T message) {
      long until = System.currentTimeMillis() + UNINTERRUPTIBLE_SEND_MILLIS;
      while (System.currentTimeMillis() < until) {
        try {
          Thread.sleep(20);
        } catch (InterruptedException e) {
          Thread.interrupted();
        }
      }
      return List.of("[\"OK\",\"ignored\",true,\"\"]");
    }

    @Override
    public <T extends BaseMessage> AutoCloseable subscribe(
        T requestMessage,
        Consumer<String> messageListener,
        Consumer<Throwable> errorListener,
        Runnable closeListener) {
      return () -> {};
    }

    @Override
    public void close() {
      // Nothing to release: this relay holds no resources.
    }
  }

  // Verifies the timeout is a budget for the whole publish, not for each relay in turn: three
  // stalled relays must not cost three times the wait the caller asked for.
  @Test
  void theTimeoutBoundsTheWholePublishNotEachRelaySeparately() throws Exception {
    List<FakeRelay> stalling = new ArrayList<>();
    RelayConnectionFactory factory =
        relayUri -> {
          if (relayUri.contains("slow")) {
            FakeRelay relay = FakeRelay.stalling(relayUri);
            stalling.add(relay);
            return relay;
          }
          return FakeRelay.accepting(relayUri);
        };
    Duration timeout = Duration.ofMillis(400);

    long startedAt = System.currentTimeMillis();
    try (RelayPool pool =
        new RelayPool(
            List.of(ACCEPTING_RELAY, "wss://relay.slow-one", "wss://relay.slow-two",
                "wss://relay.slow-three"),
            factory,
            timeout)) {

      PublishResult result = pool.publish(signedEvent());
      long elapsed = System.currentTimeMillis() - startedAt;

      assertEquals(List.of(ACCEPTING_RELAY), result.getAcceptingRelays());
      assertTrue(
          elapsed < timeout.toMillis() * 2,
          "publish took " + elapsed + "ms for a " + timeout.toMillis()
              + "ms budget, so the timeout is being spent per relay");
    } finally {
      stalling.forEach(FakeRelay::release);
    }
  }

  // Verifies a relay that cannot be reached at publish time is reported as unreachable rather
  // than as a timeout, so a down relay is distinguishable from a slow one.
  @Test
  void aRelayThatCannotBeReachedIsReportedAsUnreachableNotTimedOut() throws Exception {
    try (RelayPool pool =
        poolOf(FakeRelay.accepting(ACCEPTING_RELAY), FakeRelay.unreachable(UNREACHABLE_RELAY))) {

      PublishResult result = pool.publish(signedEvent());

      assertEquals(
          RelayPublishOutcome.Status.UNREACHABLE,
          result.findOutcome(UNREACHABLE_RELAY).orElseThrow().status());
    }
  }

  // Verifies an OK naming a different event is not credited to this publish, so one event's
  // acceptance cannot be reported as another's.
  @Test
  void anOkForAnotherEventIsNotCreditedToThisPublish() throws Exception {
    try (RelayPool pool = poolOf(FakeRelay.acknowledgingOtherEvents(ACCEPTING_RELAY))) {

      NoRelayAcceptedException thrown =
          assertThrows(NoRelayAcceptedException.class, () -> pool.publish(signedEvent()));

      assertEquals(
          RelayPublishOutcome.Status.TIMED_OUT,
          thrown.getPublishResult().findOutcome(ACCEPTING_RELAY).orElseThrow().status());
    }
  }

  // Verifies closing the pool closes every relay it opened.
  @Test
  void closingThePoolClosesEveryRelay() throws Exception {
    FakeRelay first = FakeRelay.accepting(ACCEPTING_RELAY);
    FakeRelay second = FakeRelay.accepting(SECOND_ACCEPTING_RELAY);

    poolOf(first, second).close();

    assertEquals(ConnectionState.CLOSED, first.getConnectionState());
    assertEquals(ConnectionState.CLOSED, second.getConnectionState());
  }

  private RelayPool poolOf(FakeRelay... relays) {
    Map<String, FakeRelay> byUri =
        Arrays.stream(relays)
            .collect(
                Collectors.toMap(
                    FakeRelay::getRelayUri,
                    relay -> relay,
                    (first, duplicate) -> first,
                    LinkedHashMap::new));
    return new RelayPool(List.copyOf(byUri.keySet()), byUri::get);
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
