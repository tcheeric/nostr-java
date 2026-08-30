package nostr.mcp.subscription;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.client.relay.RelayPool;
import nostr.event.filter.EventFilter;
import nostr.mcp.tool.ToolFailure;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Owns every open subscription, and closes the ones nobody is reading.
 *
 * <p>Subscriptions are the module's only long-lived resource, and each one holds a relay
 * subscription and a buffer. An agent's session ends whenever its user closes a window, without
 * telling this server, so without reaping an abandoned conversation would leak relay traffic and
 * memory for as long as the process ran. The idle timeout is what makes them safe to hand out.
 *
 * <p>The total is capped for the same reason: a model in a loop opening subscriptions is an
 * ordinary failure, and a cap turns it into an error the agent can see rather than a server that
 * slowly stops responding.
 */
@Slf4j
public final class SubscriptionRegistry implements AutoCloseable {

  private final Map<String, LiveSubscription> subscriptionsById = new ConcurrentHashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);
  private final ScheduledExecutorService reaper =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> Thread.ofVirtual().name("nostr-mcp-subscription-reaper").unstarted(runnable));
  private final RelayPool relayPool;
  private final SubscriptionLimits limits;
  private final Clock clock;
  private final Consumer<String> onSubscriptionUpdated;

  /**
   * @param relayPool the relays to subscribe across
   * @param limits how many subscriptions, how deep, and how long idle
   * @param clock the source of time for idleness
   * @param onSubscriptionUpdated called with the id when events arrive, so a host can be notified
   */
  public SubscriptionRegistry(
      @NonNull RelayPool relayPool,
      @NonNull SubscriptionLimits limits,
      @NonNull Clock clock,
      @NonNull Consumer<String> onSubscriptionUpdated) {
    this.relayPool = relayPool;
    this.limits = limits;
    this.clock = clock;
    this.onSubscriptionUpdated = onSubscriptionUpdated;
    scheduleReaping();
  }

  /**
   * Open a subscription across every relay.
   *
   * @param filter what to watch for
   * @return the new subscription, whose backlog has not yet drained
   * @throws nostr.mcp.tool.ToolException when the cap is reached or no relay accepted it
   */
  public LiveSubscription open(@NonNull EventFilter filter) {
    refuseIfAtCapacity();
    String id = "sub-" + nextId.getAndIncrement();
    LiveSubscription subscription =
        new LiveSubscription(
            id, filter, limits.bufferCapacity(), clock, () -> onSubscriptionUpdated.accept(id));
    subscription.attach(subscribeOrFail(filter, subscription));
    subscriptionsById.put(id, subscription);
    log.info("Opened subscription {} across {}", id, subscription.subscribedRelays());
    return subscription;
  }

  /**
   * Find an open subscription.
   *
   * @param id the handle the agent was given
   * @return the subscription
   * @throws nostr.mcp.tool.ToolException when no such subscription is open
   */
  public LiveSubscription require(@NonNull String id) {
    LiveSubscription subscription = subscriptionsById.get(id);
    if (subscription == null) {
      throw ToolFailure.SUBSCRIPTION_UNKNOWN.raise(
          "No subscription called '"
              + id
              + "'. It may have been closed, or reaped after being idle. Open: "
              + subscriptionsById.keySet());
    }
    return subscription;
  }

  /**
   * Every open subscription.
   *
   * @return the subscriptions, oldest first
   */
  public List<LiveSubscription> list() {
    return subscriptionsById.values().stream()
        .sorted(java.util.Comparator.comparing(LiveSubscription::id))
        .toList();
  }

  /**
   * Close a subscription and free its buffer.
   *
   * @param id the subscription to close
   * @return the closed subscription
   * @throws nostr.mcp.tool.ToolException when no such subscription is open
   */
  public LiveSubscription close(@NonNull String id) {
    LiveSubscription subscription = require(id);
    subscription.close();
    subscriptionsById.remove(id);
    log.info("Closed subscription {}", id);
    return subscription;
  }

  /**
   * Look up a subscription without failing.
   *
   * @param id the subscription to find
   * @return the subscription, or empty when it is not open
   */
  public Optional<LiveSubscription> find(@NonNull String id) {
    return Optional.ofNullable(subscriptionsById.get(id));
  }

  private void refuseIfAtCapacity() {
    if (subscriptionsById.size() >= limits.maxSubscriptions()) {
      throw ToolFailure.SUBSCRIPTION_LIMIT_REACHED.raise(
          "This server already has "
              + limits.maxSubscriptions()
              + " open subscriptions, which is the limit. Close one with nostr_unsubscribe"
              + " first. Open: "
              + subscriptionsById.keySet());
    }
  }

  private nostr.client.relay.RelaySubscription subscribeOrFail(
      EventFilter filter, LiveSubscription listener) {
    nostr.client.relay.RelaySubscription subscription =
        relayPool.subscribe(List.of(filter), listener);
    if (subscription.getSubscribedRelays().isEmpty()) {
      subscription.close();
      throw ToolFailure.RELAY_UNREACHABLE.raise(
          "No relay accepted the subscription; none of the configured relays is connected");
    }
    return subscription;
  }

  /**
   * Closes subscriptions nobody has read for a while.
   *
   * <p>Idleness is measured from the last read rather than the last event, because a subscription
   * that is receiving events nobody collects is exactly the abandoned case: a busy filter would
   * otherwise keep a forgotten subscription alive indefinitely.
   */
  private void scheduleReaping() {
    Duration interval = limits.idleTimeout().dividedBy(2);
    reaper.scheduleWithFixedDelay(
        this::reapIdleSubscriptions,
        interval.toMillis(),
        Math.max(interval.toMillis(), 1),
        TimeUnit.MILLISECONDS);
  }

  private void reapIdleSubscriptions() {
    try {
      subscriptionsById.values().stream()
          .filter(subscription -> subscription.idleMillis() > limits.idleTimeout().toMillis())
          .map(LiveSubscription::id)
          .toList()
          .forEach(
              id -> {
                log.info("Reaping subscription {} after {} idle", id, limits.idleTimeout());
                close(id);
              });
    } catch (RuntimeException e) {
      log.warn("Could not reap idle subscriptions: {}", e.getMessage());
    }
  }

  @Override
  public void close() {
    reaper.shutdownNow();
    subscriptionsById.values().forEach(LiveSubscription::close);
    subscriptionsById.clear();
  }
}
