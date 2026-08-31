package nostr.mcp.query;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.client.relay.RelayPool;
import nostr.client.relay.RelaySubscription;
import nostr.client.relay.SubscriptionListener;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.mcp.tool.ToolFailure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Runs a one-shot query and returns what the relays had.
 *
 * <p>The SDK offers subscriptions, not queries: {@code subscribe} returns immediately and events
 * arrive later on transport threads. A tool call has to answer once, so this collects until the
 * end-of-stored-events signal, then unsubscribes. That translation belongs in one place, because
 * every read tool needs it and getting the termination conditions wrong is how a tool call hangs.
 *
 * <p>Bounded twice over, and both bounds matter. A relay serves one request at a time, so an
 * unbounded query stalls every other tool call behind it; and an agent's context is finite, so
 * ten thousand events would be useless even if they arrived. Reaching either bound is reported
 * rather than hidden, since an agent that does not know its answer was truncated will draw
 * conclusions from a partial view.
 */
@Slf4j
public final class EventQuery {

  private final RelayPool relayPool;

  /**
   * @param relayPool the relays to ask
   */
  public EventQuery(@NonNull RelayPool relayPool) {
    this.relayPool = relayPool;
  }

  /**
   * Collect the events matching a filter.
   *
   * @param filter what to ask for
   * @param maxEvents the most events to return
   * @param timeout how long to wait for the relays to finish replaying
   * @return the events, newest first, and whether the answer was cut short
   * @throws nostr.mcp.tool.ToolException when no relay could be reached
   */
  public QueryResult run(@NonNull EventFilter filter, int maxEvents, @NonNull java.time.Duration timeout) {
    Collector collector = new Collector(maxEvents);
    try (RelaySubscription subscription = subscribe(filter, collector)) {
      boolean backlogDrained = collector.awaitCompletion(timeout);
      return collector.result(backlogDrained, subscription.getSubscribedRelays().size());
    }
  }

  private RelaySubscription subscribe(EventFilter filter, SubscriptionListener listener) {
    try {
      RelaySubscription subscription = relayPool.subscribe(List.of(filter), listener);
      if (subscription.getSubscribedRelays().isEmpty()) {
        subscription.close();
        throw ToolFailure.RELAY_UNREACHABLE.raise(
            "No relay accepted the query; none of the configured relays is currently connected");
      }
      return subscription;
    } catch (RuntimeException e) {
      if (e instanceof nostr.mcp.tool.ToolException) {
        throw e;
      }
      throw ToolFailure.RELAY_UNREACHABLE.raise("Could not query any relay: " + e.getMessage());
    }
  }

  /**
   * Gathers events off the transport threads until the relays finish or a bound is reached.
   *
   * <p>Events arrive on whichever thread the transport dispatches them from, so the list is
   * guarded and the waiting is done with a latch rather than by polling.
   */
  private static final class Collector implements SubscriptionListener {

    private final List<GenericEvent> events = new ArrayList<>();
    private final CountDownLatch finished = new CountDownLatch(1);
    private final int maxEvents;
    private boolean truncated;

    private Collector(int maxEvents) {
      this.maxEvents = maxEvents;
    }

    /**
     * Keeps one event beyond the limit as evidence that more exist.
     *
     * <p>The extra event is never returned. It is the only way to distinguish "the relay held
     * exactly this many" from "there were more and I stopped", because a relay that honours the
     * filter's own limit stops sending at exactly the boundary and its end-of-stored-events
     * signal looks identical in both cases.
     */
    @Override
    public void onEvent(GenericEvent event) {
      synchronized (events) {
        if (events.size() >= maxEvents) {
          truncated = true;
          finished.countDown();
          return;
        }
        events.add(event);
      }
    }

    @Override
    public void onEndOfStoredEvents() {
      finished.countDown();
    }

    @Override
    public void onRelayFailure(String relayUri, Throwable failure) {
      log.debug("Relay {} failed during a query: {}", relayUri, failure.toString());
    }

    private boolean awaitCompletion(java.time.Duration timeout) {
      try {
        return finished.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
      }
    }

    /**
     * Reports what was collected, newest first.
     *
     * <p>Sorted here rather than trusted from the relays: NIP-01 asks relays to send stored
     * events newest first, but a query spanning several relays interleaves their streams, so the
     * combined order is only what this makes it.
     */
    private QueryResult result(boolean backlogDrained, int relayCount) {
      synchronized (events) {
        List<GenericEvent> ordered =
            events.stream()
                .sorted(Comparator.comparing(GenericEvent::getCreatedAt, Comparator.reverseOrder()))
                .toList();
        return new QueryResult(ordered, truncated, !backlogDrained, relayCount);
      }
    }
  }
}
