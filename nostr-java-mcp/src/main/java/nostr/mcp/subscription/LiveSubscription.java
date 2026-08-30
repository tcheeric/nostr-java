package nostr.mcp.subscription;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.client.relay.RelaySubscription;
import nostr.client.relay.SubscriptionListener;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One open subscription, and everything the agent needs to know about its health.
 *
 * <p>A subscription is the only stateful thing this module keeps, because "watch my mentions"
 * cannot be answered by a call that returns. It holds a buffer for what arrived, a record of
 * which relays have failed, and the time it was last read, which is what lets an abandoned one
 * be reaped.
 */
@Slf4j
public final class LiveSubscription implements AutoCloseable, SubscriptionListener {

  private final Map<String, String> failuresByRelay = new ConcurrentHashMap<>();
  private final AtomicBoolean backlogDrained = new AtomicBoolean();
  private final String id;
  private final EventFilter filter;
  private final EventBuffer buffer;
  private final Clock clock;
  private final Runnable onEventArrived;
  private volatile RelaySubscription relaySubscription;
  private volatile long lastReadAtMillis;

  /**
   * @param id the handle an agent uses to name this subscription
   * @param filter what it asked for
   * @param bufferCapacity how many events to hold between polls
   * @param clock the source of time for idleness
   * @param onEventArrived called when an event lands, so a host can be notified
   */
  public LiveSubscription(
      @NonNull String id,
      @NonNull EventFilter filter,
      int bufferCapacity,
      @NonNull Clock clock,
      @NonNull Runnable onEventArrived) {
    this.id = id;
    this.filter = filter;
    this.buffer = new EventBuffer(bufferCapacity);
    this.clock = clock;
    this.onEventArrived = onEventArrived;
    this.lastReadAtMillis = clock.millis();
  }

  /**
   * Attach the relay-level subscription this one is fed by.
   *
   * @param relaySubscription the SDK subscription to close when this one closes
   */
  public void attach(@NonNull RelaySubscription relaySubscription) {
    this.relaySubscription = relaySubscription;
  }

  @Override
  public void onEvent(GenericEvent event) {
    buffer.add(event);
    onEventArrived.run();
  }

  @Override
  public void onEndOfStoredEvents() {
    backlogDrained.set(true);
  }

  /**
   * Records a relay dropping out, so degraded coverage is visible.
   *
   * <p>A subscription across three relays that is quietly down to one still returns events, and
   * an agent with no way to see that will read the thinner feed as the whole story.
   */
  @Override
  public void onRelayFailure(String relayUri, Throwable failure) {
    failuresByRelay.put(relayUri, failure.getMessage() == null ? failure.toString() : failure.getMessage());
    log.debug("Subscription {} lost relay {}: {}", id, relayUri, failure.toString());
  }

  /**
   * Take the events that have arrived, leaving the buffer empty.
   *
   * @return what was waiting
   */
  public List<GenericEvent> drain() {
    lastReadAtMillis = clock.millis();
    return buffer.drain();
  }

  /**
   * The handle an agent names this subscription by.
   *
   * @return the subscription id
   */
  public String id() {
    return id;
  }

  /**
   * What this subscription asked for.
   *
   * @return the filter
   */
  public EventFilter filter() {
    return filter;
  }

  /**
   * Whether every relay has finished replaying its stored events.
   *
   * <p>False does not mean empty. The SDK's subscribe returns before any stored event arrives,
   * so an agent that reads immediately may legitimately see nothing yet, and this flag is how it
   * tells that from a filter that matches nothing.
   *
   * @return true once the backlog has drained
   */
  public boolean backlogDrained() {
    return backlogDrained.get();
  }

  /**
   * How many events are waiting to be read.
   *
   * @return the buffer depth
   */
  public int depth() {
    return buffer.depth();
  }

  /**
   * How many events were dropped because the buffer was full.
   *
   * @return the total dropped
   */
  public long droppedCount() {
    return buffer.droppedCount();
  }

  /**
   * The relays currently feeding this subscription.
   *
   * @return the relay URIs
   */
  public Set<String> subscribedRelays() {
    return relaySubscription == null ? Set.of() : relaySubscription.getSubscribedRelays();
  }

  /**
   * The relays that have failed, and why.
   *
   * @return failure messages by relay URI
   */
  public Map<String, String> failures() {
    return Map.copyOf(failuresByRelay);
  }

  /**
   * How long since the agent last read this subscription.
   *
   * @return the idle time in milliseconds
   */
  public long idleMillis() {
    return clock.millis() - lastReadAtMillis;
  }

  @Override
  public void close() {
    if (relaySubscription != null) {
      relaySubscription.close();
    }
    buffer.drain();
  }
}
