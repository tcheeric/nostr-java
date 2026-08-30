package nostr.client.relay;

import nostr.client.springwebsocket.ConnectionState;
import nostr.client.springwebsocket.RelayTimeoutException;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the {@link FakeRelay} fixture can express the relay behaviours that coordinating code
 * must handle, so later work can be tested without WebSockets, Docker, or mocking.
 */
class FakeRelayTest {

  // Verifies a subscriber receives multiple emitted payloads and stops after its handle closes,
  // mirroring NostrRelayClientSubscriptionTest but without mocking a WebSocketSession.
  @Test
  void subscriberReceivesPayloadsUntilItsHandleIsClosed() throws Exception {
    FakeRelay relay = FakeRelay.accepting("wss://relay.one");
    List<String> received = new ArrayList<>();
    AtomicBoolean errored = new AtomicBoolean(false);

    AutoCloseable handle =
        relay.subscribe(new ReqMessage("sub"), received::add, error -> errored.set(true), null);

    relay.emit("event-one");
    relay.emit("event-two");

    assertEquals(List.of("event-one", "event-two"), received);
    assertFalse(errored.get());
    assertEquals(List.of("sub"), relay.getSentSubscriptionIds());

    handle.close();
    relay.emit("event-three");

    assertEquals(List.of("event-one", "event-two"), received);
    assertEquals(0, relay.getActiveSubscriberCount());
  }

  // Verifies an accepting relay reports success while a rejecting relay returns its reason
  // verbatim, which is what per-relay publish outcomes are built from.
  @Test
  void acceptingAndRejectingRelaysAreDistinguishable() throws Exception {
    FakeRelay accepting = FakeRelay.accepting("wss://relay.one");
    FakeRelay rejecting = FakeRelay.rejecting("wss://relay.two", "blocked: pubkey banned");

    assertTrue(accepting.send(new ReqMessage("sub")).getFirst().contains("true"));

    String rejection = rejecting.send(new ReqMessage("sub")).getFirst();
    assertTrue(rejection.contains("false"));
    assertTrue(rejection.contains("blocked: pubkey banned"));
  }

  // Verifies a silent relay reports a relay timeout, the same typed failure real transport
  // raises, so callers can tell a slow relay from an unreachable one.
  @Test
  void silentRelayTimesOutInsteadOfAnswering() {
    FakeRelay relay = FakeRelay.silent("wss://relay.three");

    assertThrows(RelayTimeoutException.class, () -> relay.send(new ReqMessage("sub")));
  }

  // Verifies an unreachable relay fails both sending and subscribing, as a down relay would.
  @Test
  void unreachableRelayRefusesSendAndSubscribe() {
    FakeRelay relay = FakeRelay.unreachable("wss://relay.four");

    assertThrows(IOException.class, () -> relay.send(new ReqMessage("sub")));
    assertThrows(
        IOException.class,
        () -> relay.subscribe(new ReqMessage("sub"), payload -> {}, error -> {}, null));
  }

  // Verifies dropping a connection mid-stream notifies subscribers and marks the relay closed,
  // which is the silent-degradation failure that recovery work must detect.
  @Test
  void droppingConnectionNotifiesSubscribersAndClosesTheRelay() throws Exception {
    FakeRelay relay = FakeRelay.accepting("wss://relay.one");
    AtomicInteger errors = new AtomicInteger();
    AtomicBoolean closed = new AtomicBoolean(false);

    relay.subscribe(
        new ReqMessage("sub"), payload -> {}, error -> errors.incrementAndGet(), () -> closed.set(true));

    relay.dropConnection();

    assertEquals(1, errors.get());
    assertTrue(closed.get());
    assertEquals(ConnectionState.CLOSED, relay.getConnectionState());
    assertThrows(IOException.class, () -> relay.send(new ReqMessage("sub")));
  }

  // Verifies a reconnected relay accepts traffic again but does not restore old subscribers,
  // so re-subscription remains the caller's responsibility to prove.
  @Test
  void reconnectingClearsSubscribersAndReopensTheRelay() throws Exception {
    FakeRelay relay = FakeRelay.accepting("wss://relay.one");
    relay.subscribe(new ReqMessage("sub"), payload -> {}, error -> {}, null);

    relay.dropConnection();
    relay.reconnect();

    assertEquals(ConnectionState.CONNECTED, relay.getConnectionState());
    assertEquals(0, relay.getActiveSubscriberCount());
    assertEquals(1, relay.send(new ReqMessage("sub")).size());
  }

  // Verifies the relay records what it was asked to send, so tests can assert which events
  // reached which relay.
  @Test
  void relayRecordsTheMessagesItWasSent() throws Exception {
    FakeRelay relay = FakeRelay.accepting("wss://relay.one");

    relay.send(new ReqMessage("first"));
    relay.send(new ReqMessage("second"));

    assertEquals(2, relay.getSentMessages().size());
  }

  // Verifies an end-of-stored-events frame can be emitted for a subscription, and that
  // withholding it simply means never calling it, which is how a stalled relay is simulated.
  @Test
  void endOfStoredEventsCanBeEmittedOrWithheld() throws Exception {
    FakeRelay replaying = FakeRelay.accepting("wss://relay.one");
    FakeRelay stalled = FakeRelay.accepting("wss://relay.two");
    List<String> fromReplaying = new ArrayList<>();
    List<String> fromStalled = new ArrayList<>();

    replaying.subscribe(new ReqMessage("sub"), fromReplaying::add, error -> {}, null);
    stalled.subscribe(new ReqMessage("sub"), fromStalled::add, error -> {}, null);

    replaying.emit("stored-event");
    replaying.emitEndOfStoredEvents("sub");
    stalled.emit("stored-event");

    assertEquals(List.of("stored-event", "[\"EOSE\",\"sub\"]"), fromReplaying);
    assertEquals(List.of("stored-event"), fromStalled);
  }

  // Verifies a subscriber registered after an earlier one was released still receives events,
  // guarding against registration identifiers being reused and silently dropping a subscriber.
  @Test
  void subscribersRegisteredAfterAReleaseStillReceiveEvents() throws Exception {
    FakeRelay relay = FakeRelay.accepting("wss://relay.one");
    List<String> first = new ArrayList<>();
    List<String> second = new ArrayList<>();

    AutoCloseable firstHandle =
        relay.subscribe(new ReqMessage("first"), first::add, error -> {}, null);
    firstHandle.close();
    relay.subscribe(new ReqMessage("second"), second::add, error -> {}, null);
    relay.subscribe(new ReqMessage("third"), payload -> {}, error -> {}, null);

    relay.emit("event");

    assertTrue(first.isEmpty());
    assertEquals(List.of("event"), second);
    assertEquals(2, relay.getActiveSubscriberCount());
  }

  // Verifies payloads addressed to one subscription reach only that subscription's listener,
  // mirroring NostrRelayClientSubscriptionRoutingTest without mocking a WebSocketSession.
  @Test
  void payloadsAreRoutedToTheirOwnSubscription() throws Exception {
    FakeRelay relay = FakeRelay.accepting("wss://relay.one");
    List<String> seenByA = new ArrayList<>();
    List<String> seenByB = new ArrayList<>();

    relay.subscribe(new ReqMessage("sub-a"), seenByA::add, error -> {}, null);
    relay.subscribe(new ReqMessage("sub-b"), seenByB::add, error -> {}, null);

    relay.emitTo("sub-a", "for-a");
    relay.emitTo("sub-b", "for-b");

    assertEquals(List.of("for-a"), seenByA);
    assertEquals(List.of("for-b"), seenByB);
  }

  // Verifies a scripted backlog is delivered in order, which is how stored-event replay is
  // arranged before an end-of-stored-events frame.
  @Test
  void scriptedEventSequenceIsDeliveredInOrder() throws Exception {
    FakeRelay relay = FakeRelay.accepting("wss://relay.one");
    List<String> received = new ArrayList<>();

    relay.subscribe(new ReqMessage("sub"), received::add, error -> {}, null);
    relay.emitAll(List.of("one", "two", "three"));

    assertEquals(List.of("one", "two", "three"), received);
  }

  // Verifies a factory can hand out scripted relays by URI, which is how coordinating code
  // receives fakes in place of real connections.
  @Test
  void factorySuppliesScriptedRelaysByUri() throws Exception {
    FakeRelay accepting = FakeRelay.accepting("wss://relay.one");
    FakeRelay rejecting = FakeRelay.rejecting("wss://relay.two", "rate-limited");
    RelayConnectionFactory factory =
        relayUri -> "wss://relay.one".equals(relayUri) ? accepting : rejecting;

    assertEquals("wss://relay.one", factory.connect("wss://relay.one").getRelayUri());
    assertTrue(factory.connect("wss://relay.two").send(new ReqMessage("s"))
        .getFirst().contains("rate-limited"));
  }
}
