package nostr.client.relay;

import lombok.extern.slf4j.Slf4j;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One subscription spread across many relays, presented to the caller as a single stream.
 *
 * <p>The subscription keeps its filter rather than forgetting it once registered, because a
 * relay that drops must be re-subscribed when it returns; a fire-and-forget handle could not do
 * that. It also tracks which relays have finished replaying stored events, so the caller gets
 * one end-of-backlog signal instead of one per relay.
 */
@Slf4j
public class RelaySubscription implements AutoCloseable {

  private final String subscriptionId;
  private final List<EventFilter> filters;
  private final SubscriptionListener listener;
  private final DeliveredEventWindow deliveredEvents;
  private final Map<String, AutoCloseable> handlesByRelay = new ConcurrentHashMap<>();
  private final Set<String> relaysAwaitingBacklog = ConcurrentHashMap.newKeySet();
  private final AtomicBoolean endOfStoredEventsAnnounced = new AtomicBoolean();
  private final AtomicBoolean closed = new AtomicBoolean();

  RelaySubscription(
      String subscriptionId,
      List<EventFilter> filters,
      SubscriptionListener listener,
      int deduplicationWindowSize) {
    this.subscriptionId = Objects.requireNonNull(subscriptionId, "subscriptionId");
    this.filters = List.copyOf(filters);
    this.listener = Objects.requireNonNull(listener, "listener");
    this.deliveredEvents = new DeliveredEventWindow(deduplicationWindowSize);
  }

  /**
   * The identifier relays see for this subscription.
   *
   * @return the subscription identifier
   */
  public String getSubscriptionId() {
    return subscriptionId;
  }

  /**
   * The relays currently feeding this subscription.
   *
   * @return the subscribed relays' URIs
   */
  public Set<String> getSubscribedRelays() {
    return Set.copyOf(handlesByRelay.keySet());
  }

  /**
   * Whether the caller has been told the stored backlog is drained.
   *
   * @return {@code true} once the single end-of-stored-events signal has fired
   */
  public boolean hasAnnouncedEndOfStoredEvents() {
    return endOfStoredEventsAnnounced.get();
  }

  /**
   * Register this subscription's filter with one relay.
   *
   * <p>Used both when the subscription opens and when a relay rejoins, which is why the filter
   * is retained rather than consumed.
   *
   * @param connection the relay to subscribe on
   * @throws IOException if the relay refused the subscription
   */
  void subscribeOn(RelayConnection connection) throws IOException {
    if (closed.get()) {
      return;
    }
    String relayUri = connection.getRelayUri();
    relaysAwaitingBacklog.add(relayUri);
    AutoCloseable handle =
        connection.subscribe(
            new ReqMessage(subscriptionId, filters),
            payload -> acceptPayload(relayUri, payload),
            failure -> reportRelayFailure(relayUri, failure),
            () -> reportRelayFailure(relayUri, new IOException("Relay " + relayUri + " closed")));
    handlesByRelay.put(relayUri, handle);
  }

  /**
   * Whether this relay is absent from the subscription and should be re-subscribed.
   *
   * @param relayUri the relay to check
   * @return {@code true} when the relay is not currently feeding this subscription
   */
  boolean isMissing(String relayUri) {
    return !closed.get() && !handlesByRelay.containsKey(relayUri);
  }

  private void acceptPayload(String relayUri, String payload) {
    if (closed.get() || payload == null) {
      return;
    }
    if (payload.startsWith("[\"EOSE\"")) {
      recordBacklogDrained(relayUri);
      return;
    }
    if (payload.startsWith("[\"EVENT\"")) {
      deliverIfNotAlreadySeen(relayUri, payload);
    }
  }

  /**
   * Parse an event and pass it on, unless another relay already delivered it.
   *
   * <p>A malformed payload is reported and discarded rather than propagated, so one relay
   * sending nonsense cannot end a subscription that the other relays are still serving.
   */
  private void deliverIfNotAlreadySeen(String relayUri, String payload) {
    GenericEvent event;
    try {
      EventMessage message = EventMessage.decode(payload);
      event = message.getEvent();
    } catch (RuntimeException e) {
      log.warn("Discarding unreadable payload from relay {}: {}", relayUri, e.getMessage());
      listener.onRelayFailure(relayUri, e);
      return;
    }
    if (event != null && deliveredEvents.isFirstSighting(event.getId())) {
      listener.onEvent(event);
    }
  }

  private void recordBacklogDrained(String relayUri) {
    relaysAwaitingBacklog.remove(relayUri);
    announceEndOfStoredEventsIfComplete();
  }

  /**
   * Emit the one end-of-backlog signal once no relay is still owed.
   *
   * <p>Also called when a relay is given up on, so a relay that never answers cannot leave the
   * caller waiting on a signal that will never come.
   */
  void announceEndOfStoredEventsIfComplete() {
    if (relaysAwaitingBacklog.isEmpty() && endOfStoredEventsAnnounced.compareAndSet(false, true)) {
      listener.onEndOfStoredEvents();
    }
  }

  /** Stop waiting for relays that have not replayed their backlog in time. */
  void stopAwaitingBacklog() {
    relaysAwaitingBacklog.clear();
    announceEndOfStoredEventsIfComplete();
  }

  private void reportRelayFailure(String relayUri, Throwable failure) {
    if (closed.get()) {
      return;
    }
    handlesByRelay.remove(relayUri);
    relaysAwaitingBacklog.remove(relayUri);
    listener.onRelayFailure(relayUri, failure);
    announceEndOfStoredEventsIfComplete();
  }

  @Override
  public void close() {
    if (!closed.compareAndSet(false, true)) {
      return;
    }
    handlesByRelay.values().forEach(this::closeQuietly);
    handlesByRelay.clear();
    relaysAwaitingBacklog.clear();
  }

  private void closeQuietly(AutoCloseable handle) {
    try {
      handle.close();
    } catch (Exception e) {
      log.warn("Failed to cancel a relay subscription: {}", e.getMessage());
    }
  }
}
