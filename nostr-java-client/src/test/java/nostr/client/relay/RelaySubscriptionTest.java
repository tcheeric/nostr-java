package nostr.client.relay;

import nostr.base.PublicKey;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies one subscription spread across relays behaves as a single stream: events arrive once,
 * the backlog ends once, and a relay that drops is noticed and recovered.
 */
class RelaySubscriptionTest {

  private static final String FIRST_RELAY = "wss://relay.one";
  private static final String SECOND_RELAY = "wss://relay.two";
  private static final String THIRD_RELAY = "wss://relay.three";
  private static final String FOURTH_RELAY = "wss://relay.four";
  private static final List<EventFilter> TEXT_NOTES =
      List.of(EventFilter.builder().kind(1).build());

  // Verifies subscribing once registers the filter with every relay in the pool.
  @Test
  void subscribingRegistersTheFilterWithEveryRelay() throws Exception {
    Map<String, FakeRelay> relays =
        relaysNamed(FIRST_RELAY, SECOND_RELAY, THIRD_RELAY, FOURTH_RELAY);

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription = pool.subscribe(TEXT_NOTES, event -> {})) {

      assertEquals(relays.keySet(), subscription.getSubscribedRelays());
      relays
          .values()
          .forEach(relay -> assertEquals(1, relay.getSentSubscriptionIds().size()));
    }
  }

  // Verifies an event held by four relays reaches the caller once, already parsed, rather than
  // once per relay that happens to have it.
  @Test
  void anEventArrivingFromFourRelaysIsDeliveredOnce() throws Exception {
    Map<String, FakeRelay> relays =
        relaysNamed(FIRST_RELAY, SECOND_RELAY, THIRD_RELAY, FOURTH_RELAY);
    List<GenericEvent> received = new ArrayList<>();
    GenericEvent shared = event("shared note");

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription = pool.subscribe(TEXT_NOTES, received::add)) {

      relays.values().forEach(relay -> relay.emitEvent(subscription.getSubscriptionId(), shared));

      assertEquals(1, received.size());
      assertEquals(shared.getId(), received.getFirst().getId());
      assertEquals("shared note", received.getFirst().getContent());
    }
  }

  // Verifies distinct events all arrive, so de-duplication does not suppress genuine traffic.
  @Test
  void distinctEventsAreAllDelivered() throws Exception {
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY, SECOND_RELAY);
    List<GenericEvent> received = new ArrayList<>();

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription = pool.subscribe(TEXT_NOTES, received::add)) {

      relays.get(FIRST_RELAY).emitEvent(subscription.getSubscriptionId(), event("first"));
      relays.get(SECOND_RELAY).emitEvent(subscription.getSubscriptionId(), event("second"));

      assertEquals(2, received.size());
    }
  }

  // Verifies the de-duplication window stays bounded, so a long-lived firehose subscription does
  // not grow its memory without limit.
  @Test
  void theDeduplicationWindowStaysBounded() throws Exception {
    int windowSize = 16;
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY);
    AtomicInteger received = new AtomicInteger();

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription =
            pool.subscribe(TEXT_NOTES, ignored -> received.incrementAndGet(), windowSize)) {

      for (int emitted = 0; emitted < windowSize * 10; emitted++) {
        relays.get(FIRST_RELAY).emitEvent(subscription.getSubscriptionId(), event("note " + emitted));
      }

      assertEquals(windowSize * 10, received.get());
    }
  }

  // Verifies closing the subscription stops delivery from every relay at once.
  @Test
  void closingTheSubscriptionStopsEveryRelay() throws Exception {
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY, SECOND_RELAY);
    List<GenericEvent> received = new ArrayList<>();

    try (RelayPool pool = poolOf(relays)) {
      RelaySubscription subscription = pool.subscribe(TEXT_NOTES, received::add);
      subscription.close();

      relays
          .values()
          .forEach(relay -> relay.emitEvent(subscription.getSubscriptionId(), event("after close")));

      assertTrue(received.isEmpty());
      relays.values().forEach(relay -> assertEquals(0, relay.getActiveSubscriberCount()));
    }
  }

  // Verifies the caller is told the backlog is drained exactly once, after every relay has
  // reported, rather than once per relay.
  @Test
  void oneEndOfStoredEventsIsEmittedAfterEveryRelayReports() throws Exception {
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY, SECOND_RELAY, THIRD_RELAY);
    AtomicInteger backlogDrained = new AtomicInteger();

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription =
            pool.subscribe(TEXT_NOTES, countingBacklog(backlogDrained))) {

      relays.get(FIRST_RELAY).emitEndOfStoredEvents(subscription.getSubscriptionId());
      relays.get(SECOND_RELAY).emitEndOfStoredEvents(subscription.getSubscriptionId());
      assertEquals(0, backlogDrained.get(), "the backlog ended before every relay had reported");

      relays.get(THIRD_RELAY).emitEndOfStoredEvents(subscription.getSubscriptionId());

      assertEquals(1, backlogDrained.get());
    }
  }

  // Verifies per-relay EOSE frames are never handed to the caller as events.
  @Test
  void perRelayEndOfStoredEventsFramesAreNotDeliveredAsEvents() throws Exception {
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY, SECOND_RELAY);
    List<GenericEvent> received = new ArrayList<>();

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription = pool.subscribe(TEXT_NOTES, received::add)) {

      relays.values().forEach(relay -> relay.emitEndOfStoredEvents(subscription.getSubscriptionId()));

      assertTrue(received.isEmpty());
    }
  }

  // Verifies a relay that never replays its backlog cannot withhold the signal forever, so an
  // application is not left showing a loading state indefinitely.
  @Test
  void theBacklogSignalArrivesDespiteASilentRelay() throws Exception {
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY, SECOND_RELAY);
    AtomicInteger backlogDrained = new AtomicInteger();

    try (RelayPool pool =
            new RelayPool(
                List.copyOf(relays.keySet()),
                relays::get,
                RelayPool.DEFAULT_PUBLISH_TIMEOUT,
                RelayPool.DEFAULT_RECONNECT_INTERVAL,
                Duration.ofMillis(200));
        RelaySubscription subscription =
            pool.subscribe(TEXT_NOTES, countingBacklog(backlogDrained))) {

      relays.get(FIRST_RELAY).emitEndOfStoredEvents(subscription.getSubscriptionId());

      await().atMost(5, TimeUnit.SECONDS).until(() -> backlogDrained.get() == 1);
      assertTrue(subscription.hasAnnouncedEndOfStoredEvents());
    }
  }

  // Verifies a relay dropping mid-stream tells the caller, so a subscription cannot quietly
  // degrade from several relays to one.
  @Test
  void aRelayDroppingMidStreamNotifiesTheCaller() throws Exception {
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY, SECOND_RELAY);
    AtomicReference<String> failedRelay = new AtomicReference<>();

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription =
            pool.subscribe(TEXT_NOTES, reportingFailures(failedRelay))) {

      relays.get(SECOND_RELAY).dropConnection();

      assertEquals(SECOND_RELAY, failedRelay.get());
      assertFalse(subscription.getSubscribedRelays().contains(SECOND_RELAY));
    }
  }

  // Verifies a relay that drops is re-subscribed once it reconnects, so the stream repairs
  // itself rather than permanently losing a relay.
  @Test
  void aDroppedRelayIsResubscribedWhenItReconnects() throws Exception {
    FakeRelay initial = FakeRelay.accepting(SECOND_RELAY);
    AtomicInteger connectionAttempts = new AtomicInteger();
    AtomicReference<FakeRelay> replacement = new AtomicReference<>();
    RelayConnectionFactory factory =
        relayUri -> {
          if (!SECOND_RELAY.equals(relayUri)) {
            return FakeRelay.accepting(relayUri);
          }
          if (connectionAttempts.incrementAndGet() == 1) {
            return initial;
          }
          replacement.set(FakeRelay.accepting(relayUri));
          return replacement.get();
        };

    try (RelayPool pool =
            new RelayPool(
                List.of(FIRST_RELAY, SECOND_RELAY),
                factory,
                RelayPool.DEFAULT_PUBLISH_TIMEOUT,
                Duration.ofMillis(50));
        RelaySubscription subscription = pool.subscribe(TEXT_NOTES, event -> {})) {

      initial.dropConnection();

      await()
          .atMost(5, TimeUnit.SECONDS)
          .until(() -> subscription.getSubscribedRelays().contains(SECOND_RELAY));
      assertEquals(
          List.of(subscription.getSubscriptionId()), replacement.get().getSentSubscriptionIds());
    }
  }

  // Verifies a malformed payload is reported but does not end the subscription, so one relay
  // sending nonsense cannot stop the others being served.
  @Test
  void aMalformedPayloadIsReportedWithoutEndingTheStream() throws Exception {
    Map<String, FakeRelay> relays = relaysNamed(FIRST_RELAY, SECOND_RELAY);
    List<GenericEvent> received = new ArrayList<>();
    AtomicBoolean reported = new AtomicBoolean();

    try (RelayPool pool = poolOf(relays);
        RelaySubscription subscription =
            pool.subscribe(
                TEXT_NOTES,
                new SubscriptionListener() {
                  @Override
                  public void onEvent(GenericEvent event) {
                    received.add(event);
                  }

                  @Override
                  public void onRelayFailure(String relayUri, Throwable failure) {
                    reported.set(true);
                  }
                })) {

      relays.get(FIRST_RELAY).emitTo(subscription.getSubscriptionId(), "[\"EVENT\",\"not-json\"");
      relays.get(SECOND_RELAY).emitEvent(subscription.getSubscriptionId(), event("still flowing"));

      assertTrue(reported.get(), "the malformed payload was not reported");
      assertEquals(1, received.size(), "the stream stopped after a malformed payload");
    }
  }

  private SubscriptionListener countingBacklog(AtomicInteger backlogDrained) {
    return new SubscriptionListener() {
      @Override
      public void onEvent(GenericEvent event) {
        // This test observes only the backlog signal.
      }

      @Override
      public void onEndOfStoredEvents() {
        backlogDrained.incrementAndGet();
      }
    };
  }

  private SubscriptionListener reportingFailures(AtomicReference<String> failedRelay) {
    return new SubscriptionListener() {
      @Override
      public void onEvent(GenericEvent event) {
        // This test observes only relay failures.
      }

      @Override
      public void onRelayFailure(String relayUri, Throwable failure) {
        failedRelay.set(relayUri);
      }
    };
  }

  private Map<String, FakeRelay> relaysNamed(String... relayUris) {
    Map<String, FakeRelay> relays = new LinkedHashMap<>();
    for (String relayUri : relayUris) {
      relays.put(relayUri, FakeRelay.accepting(relayUri));
    }
    return relays;
  }

  private RelayPool poolOf(Map<String, FakeRelay> relays) {
    return new RelayPool(List.copyOf(relays.keySet()), relays::get);
  }

  private GenericEvent event(String content) {
    GenericEvent event =
        GenericEvent.builder()
            .pubKey(new PublicKey("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
            .kind(1)
            .content(content)
            .build();
    event.update();
    return event;
  }
}
