package nostr.client.relay;

import lombok.extern.slf4j.Slf4j;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.client.springwebsocket.ConnectionState;
import nostr.client.springwebsocket.RelayTimeoutException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A set of relay connections that one event can be published to at once.
 *
 * <p>Nostr is a multi-relay protocol, but a single connection can only ever give a single
 * answer. This pool fans a publish out across its members concurrently and collects what each
 * one said into a {@link PublishResult}, so partial delivery is visible rather than hidden
 * behind a boolean.
 *
 * <p>Construction is <strong>best effort</strong>: relays that cannot be reached are recorded as
 * down rather than aborting the pool, because one dead relay must never stop an application
 * starting. A publish waits for every relay's answer up to {@link #DEFAULT_PUBLISH_TIMEOUT},
 * after which silent relays are recorded as timed out, so one slow relay cannot hang the call.
 */
@Slf4j
public class RelayPool implements AutoCloseable {

  /** How long a publish waits for a relay before recording it as timed out. */
  public static final Duration DEFAULT_PUBLISH_TIMEOUT = Duration.ofSeconds(10);

  /** How often downed relays are retried in the background. */
  public static final Duration DEFAULT_RECONNECT_INTERVAL = Duration.ofSeconds(30);

  /** How long a subscription waits for every relay to replay its backlog. */
  public static final Duration DEFAULT_BACKLOG_TIMEOUT = Duration.ofSeconds(10);

  /**
   * Live connections, keyed by relay URI.
   *
   * <p>Concurrent rather than ordered because membership changes while publishes are in flight:
   * a recovering relay rejoins from another thread, and iterating a plain map at that moment
   * throws. Configured order is preserved separately in {@link #configuredRelayUris}, so results
   * stay predictable without making readers pay for a lock.
   */
  private final Map<String, RelayConnection> connectionsByRelay = new ConcurrentHashMap<>();

  private final Map<String, String> unreachableRelays = new ConcurrentHashMap<>();

  /**
   * The relays in the pool, in the order they joined, so outcomes are reported predictably.
   *
   * <p>Copy-on-write because membership changes while publishes iterate it: a relay added for a
   * single delivery, or one rejoining after an outage, must not disturb work already in flight.
   */
  private final List<String> configuredRelayUris = new CopyOnWriteArrayList<>();

  /**
   * How many callers still need each transiently added relay.
   *
   * <p>A relay added to deliver one message must be released afterwards, but two deliveries to
   * the same recipient may overlap. Counting holders means the second does not close the
   * connection the first is still using.
   */
  private final Map<String, AtomicInteger> transientRelayHolders = new ConcurrentHashMap<>();

  /**
   * Retries downed relays on a schedule the pool owns.
   *
   * <p>Relays go down and come back, and an application must not need restarting to notice, so
   * reconnection belongs to the pool rather than to whoever remembers to call it.
   */
  private final ScheduledExecutorService reconnectScheduler;

  /** Live subscriptions, kept so relays that rejoin can be re-subscribed from their filters. */
  private final Set<RelaySubscription> subscriptions = ConcurrentHashMap.newKeySet();

  private final Duration backlogTimeout;
  private final AtomicLong subscriptionSequence = new AtomicLong();
  /**
   * One lock per relay, because a connection serves one request at a time.
   *
   * <p>{@code NostrRelayClient} rejects a second concurrent {@code send} on the same connection,
   * so without this two callers publishing at once would collide. Locking per relay rather than
   * across the pool keeps fan-out concurrent: a slow relay delays only its own queue.
   */
  private final Map<String, ReentrantLock> locksByRelay = new ConcurrentHashMap<>();
  private final RelayConnectionFactory connectionFactory;
  private final Duration publishTimeout;
  private final BaseMessageDecoder<OkMessage> okMessageDecoder = new BaseMessageDecoder<>();

  /**
   * Connect to each relay, keeping those that answer.
   *
   * @param relayUris the relays to connect to
   * @param connectionFactory opens a connection for a relay URI
   * @param publishTimeout how long a publish waits before recording a relay as timed out
   */
  public RelayPool(
      List<String> relayUris,
      RelayConnectionFactory connectionFactory,
      Duration publishTimeout) {
    this(relayUris, connectionFactory, publishTimeout, DEFAULT_RECONNECT_INTERVAL);
  }

  /**
   * Connect to each relay, retrying the ones that are down at the given interval.
   *
   * @param relayUris the relays to connect to
   * @param connectionFactory opens a connection for a relay URI
   * @param publishTimeout how long a publish waits before recording a relay as timed out
   * @param reconnectInterval how often downed relays are retried in the background
   */
  public RelayPool(
      List<String> relayUris,
      RelayConnectionFactory connectionFactory,
      Duration publishTimeout,
      Duration reconnectInterval) {
    this(relayUris, connectionFactory, publishTimeout, reconnectInterval, DEFAULT_BACKLOG_TIMEOUT);
  }

  /**
   * Connect to each relay, choosing every timing the pool observes.
   *
   * @param relayUris the relays to connect to
   * @param connectionFactory opens a connection for a relay URI
   * @param publishTimeout how long a publish waits before recording a relay as timed out
   * @param reconnectInterval how often downed relays are retried in the background
   * @param backlogTimeout how long a subscription waits for every relay to replay stored events
   */
  public RelayPool(
      List<String> relayUris,
      RelayConnectionFactory connectionFactory,
      Duration publishTimeout,
      Duration reconnectInterval,
      Duration backlogTimeout) {
    this.backlogTimeout = Objects.requireNonNull(backlogTimeout, "backlogTimeout");
    Objects.requireNonNull(relayUris, "relayUris");
    Objects.requireNonNull(connectionFactory, "connectionFactory");
    this.publishTimeout = Objects.requireNonNull(publishTimeout, "publishTimeout");
    this.connectionFactory = connectionFactory;
    this.configuredRelayUris.addAll(relayUris);
    relayUris.forEach(relayUri -> connectOrRecordAsDown(relayUri, connectionFactory));
    this.reconnectScheduler = startReconnecting(reconnectInterval);
  }

  private ScheduledExecutorService startReconnecting(Duration reconnectInterval) {
    Objects.requireNonNull(reconnectInterval, "reconnectInterval");
    ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(
            runnable -> Thread.ofVirtual().name("nostr-relay-reconnect").unstarted(runnable));
    scheduler.scheduleWithFixedDelay(
        this::reconnectDownedRelaysQuietly,
        reconnectInterval.toMillis(),
        reconnectInterval.toMillis(),
        TimeUnit.MILLISECONDS);
    return scheduler;
  }

  private void reconnectDownedRelaysQuietly() {
    try {
      retryUnreachableRelays();
    } catch (RuntimeException e) {
      log.warn("Background relay reconnection failed: {}", e.getMessage());
    }
  }

  /**
   * Connect to each relay using the default publish timeout.
   *
   * @param relayUris the relays to connect to
   * @param connectionFactory opens a connection for a relay URI
   */
  public RelayPool(List<String> relayUris, RelayConnectionFactory connectionFactory) {
    this(relayUris, connectionFactory, DEFAULT_PUBLISH_TIMEOUT);
  }

  private void connectOrRecordAsDown(String relayUri, RelayConnectionFactory connectionFactory) {
    try {
      connectionsByRelay.put(relayUri, connectionFactory.connect(relayUri));
    } catch (IOException e) {
      log.warn("Relay {} is unreachable and was not added to the pool: {}", relayUri, e.getMessage());
      unreachableRelays.put(relayUri, e.getMessage());
    }
  }

  /**
   * Publish an event to every connected relay and report what each one did.
   *
   * @param event the signed event to publish
   * @return each relay's outcome
   * @throws NoRelayAcceptedException when not one relay stored the event
   */
  public PublishResult publish(GenericEvent event) throws NoRelayAcceptedException {
    Objects.requireNonNull(event, "event");
    PublishResult result = PublishResult.of(event.getId(), collectOutcomes(event));
    if (!result.isAccepted()) {
      throw new NoRelayAcceptedException(result);
    }
    return result;
  }

  private List<RelayPublishOutcome> collectOutcomes(GenericEvent event) {
    List<RelayPublishOutcome> outcomes = new ArrayList<>(sendConcurrently(event));
    unreachableRelays.forEach(
        (relayUri, reason) -> outcomes.add(RelayPublishOutcome.unreachable(relayUri, reason)));
    return outcomes;
  }

  /**
   * Send to every relay at once and gather what each one said.
   *
   * <p>A relay that misses the timeout has its task cancelled, which interrupts the sending
   * thread. A transport that ignores interruption keeps running after this method returns: the
   * caller is still protected, because the timeout is honoured regardless, but the thread is
   * orphaned until its own I/O gives up. This is acceptable because the alternative, waiting for
   * it, would let one stuck relay defeat the timeout that exists to bound exactly that.
   */
  private List<RelayPublishOutcome> sendConcurrently(GenericEvent event) {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Map<String, Future<RelayPublishOutcome>> pending = new LinkedHashMap<>();
      configuredRelayUris.forEach(
          relayUri -> {
            RelayConnection connection = connectionsByRelay.get(relayUri);
            if (connection != null) {
              pending.put(relayUri, executor.submit(sendTo(connection, event)));
            }
          });
      Instant deadline = Instant.now().plus(publishTimeout);
      return pending.entrySet().stream()
          .map(entry -> awaitOutcome(entry.getKey(), entry.getValue(), deadline))
          .toList();
    }
  }

  private Callable<RelayPublishOutcome> sendTo(RelayConnection connection, GenericEvent event) {
    return () -> {
      ReentrantLock relayLock =
          locksByRelay.computeIfAbsent(connection.getRelayUri(), uri -> new ReentrantLock(true));
      relayLock.lock();
      try {
        return interpretResponses(
            connection.getRelayUri(), event.getId(), connection.send(new EventMessage(event)));
      } catch (RelayTimeoutException e) {
        return RelayPublishOutcome.timedOut(connection.getRelayUri(), e.getMessage());
      } catch (IOException e) {
        return RelayPublishOutcome.unreachable(connection.getRelayUri(), e.getMessage());
      } finally {
        relayLock.unlock();
      }
    };
  }

  /**
   * Wait for one relay's answer, but never past the deadline the whole publish shares.
   *
   * <p>The budget belongs to the publish, not to each relay: spending the full timeout on each
   * relay in turn would let five stalled relays cost five times the wait the caller asked for.
   */
  private RelayPublishOutcome awaitOutcome(
      String relayUri, Future<RelayPublishOutcome> pending, Instant deadline) {
    long remainingMillis = Math.max(0, Duration.between(Instant.now(), deadline).toMillis());
    try {
      return pending.get(remainingMillis, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      pending.cancel(true);
      return RelayPublishOutcome.timedOut(
          relayUri, "No answer within " + publishTimeout.toMillis() + "ms");
    } catch (ExecutionException e) {
      return RelayPublishOutcome.timedOut(relayUri, String.valueOf(e.getCause()));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return RelayPublishOutcome.timedOut(relayUri, "Interrupted while awaiting relay answer");
    }
  }

  /**
   * Read the relay's answer for one specific event.
   *
   * <p>A connection can carry answers for more than one event, so the {@code OK} is matched on
   * the event id rather than taken as the first {@code OK} seen: crediting another event's
   * acceptance to this one would report a publish that never happened.
   */
  private RelayPublishOutcome interpretResponses(
      String relayUri, String eventId, List<String> responses) {
    return responses.stream()
        .filter(payload -> payload != null && payload.startsWith("[\"OK\""))
        .map(this::decodeOkMessage)
        .filter(okMessage -> okMessage != null && eventId.equals(okMessage.getEventId()))
        .findFirst()
        .map(okMessage -> describeOutcome(relayUri, okMessage))
        .orElseGet(
            () -> RelayPublishOutcome.timedOut(relayUri, "Relay sent no OK for event " + eventId));
  }

  private OkMessage decodeOkMessage(String payload) {
    try {
      return okMessageDecoder.decode(payload);
    } catch (RuntimeException e) {
      log.warn("Ignoring unreadable OK from relay: {}", e.getMessage());
      return null;
    }
  }

  private RelayPublishOutcome describeOutcome(String relayUri, OkMessage okMessage) {
    return Boolean.TRUE.equals(okMessage.getFlag())
        ? RelayPublishOutcome.accepted(relayUri)
        : RelayPublishOutcome.rejected(relayUri, okMessage.getMessage());
  }

  /**
   * The relays currently connected.
   *
   * @return the connected relays' URIs, in the order they were configured
   */
  public List<String> getConnectedRelays() {
    return configuredRelayUris.stream()
        .filter(
            relayUri ->
                getConnectionState(relayUri).orElse(null) == ConnectionState.CONNECTED)
        .toList();
  }

  /**
   * One relay's connection state.
   *
   * @param relayUri the relay to look up
   * @return that relay's state, or empty when the relay is not in the pool
   */
  public Optional<ConnectionState> getConnectionState(String relayUri) {
    return Optional.ofNullable(connectionsByRelay.get(relayUri))
        .map(RelayConnection::getConnectionState);
  }

  /**
   * Try the relays that were unreachable again, returning any that now answer to service.
   *
   * <p>Relays go down and come back, and an application should not need restarting to notice.
   * Callers decide when to retry; the pool does not impose a schedule of its own.
   *
   * @return the relays that rejoined
   */
  public List<String> retryUnreachableRelays() {
    markDroppedRelaysAsUnreachable();
    List<String> rejoined = new ArrayList<>();
    getUnreachableRelays()
        .forEach(
            relayUri -> {
              try {
                RelayConnection reconnected = connectionFactory.connect(relayUri);
                connectionsByRelay.put(relayUri, reconnected);
                unreachableRelays.remove(relayUri);
                rejoined.add(relayUri);
                resumeSubscriptionsOn(reconnected);
                log.info("Relay {} recovered and rejoined the pool", relayUri);
              } catch (IOException e) {
                log.debug("Relay {} is still unreachable: {}", relayUri, e.getMessage());
              }
            });
    return List.copyOf(rejoined);
  }

  /**
   * Add a relay to the running pool, connecting it if it is not already a member.
   *
   * <p>Relay sets are not fixed: a user changes their preferences, and a direct message must be
   * delivered to relays the recipient chose rather than the ones this pool was built with.
   * Adding an existing relay records another holder rather than opening a second connection.
   *
   * @param relayUri the relay to add
   * @return {@code true} if the relay is now available for use
   */
  public boolean addRelay(String relayUri) {
    Objects.requireNonNull(relayUri, "relayUri");
    transientRelayHolders.computeIfAbsent(relayUri, uri -> new AtomicInteger()).incrementAndGet();
    if (connectionsByRelay.containsKey(relayUri)) {
      return true;
    }
    if (!configuredRelayUris.contains(relayUri)) {
      configuredRelayUris.add(relayUri);
    }
    connectOrRecordAsDown(relayUri, connectionFactory);
    RelayConnection connection = connectionsByRelay.get(relayUri);
    if (connection == null) {
      return false;
    }
    resumeSubscriptionsOn(connection);
    return true;
  }

  /**
   * Release a relay added with {@link #addRelay}, closing it once no caller still needs it.
   *
   * <p>The connection survives while another holder remains, so overlapping deliveries to the
   * same relay do not cut each other off.
   *
   * @param relayUri the relay to release
   * @return {@code true} if the relay was closed and removed from the pool
   */
  public boolean releaseRelay(String relayUri) {
    Objects.requireNonNull(relayUri, "relayUri");
    AtomicInteger holders = transientRelayHolders.get(relayUri);
    if (holders != null && holders.decrementAndGet() > 0) {
      return false;
    }
    transientRelayHolders.remove(relayUri);
    return removeRelay(relayUri);
  }

  /**
   * Remove a relay from the pool immediately, closing its connection.
   *
   * @param relayUri the relay to remove
   * @return {@code true} if the relay was in the pool
   */
  public boolean removeRelay(String relayUri) {
    Objects.requireNonNull(relayUri, "relayUri");
    configuredRelayUris.remove(relayUri);
    unreachableRelays.remove(relayUri);
    transientRelayHolders.remove(relayUri);
    locksByRelay.remove(relayUri);
    RelayConnection removed = connectionsByRelay.remove(relayUri);
    if (removed == null) {
      return false;
    }
    closeQuietly(removed);
    return true;
  }

  /**
   * The relays currently in the pool, whether connected or awaiting reconnection.
   *
   * @return the member relays' URIs, in the order they joined
   */
  public List<String> getRelays() {
    return List.copyOf(configuredRelayUris);
  }

  /**
   * Open a subscription on every relay in the pool, delivered as one de-duplicated stream.
   *
   * <p>The subscription is retained by the pool so that a relay which drops and later recovers
   * is re-subscribed from the same filter, rather than silently ceasing to contribute.
   *
   * @param filters what to subscribe to
   * @param listener receives events, the end-of-backlog signal, and per-relay failures
   * @return the subscription, which unsubscribes from every relay when closed
   */
  public RelaySubscription subscribe(List<EventFilter> filters, SubscriptionListener listener) {
    return subscribe(filters, listener, DeliveredEventWindow.DEFAULT_CAPACITY);
  }

  /**
   * Open a subscription, choosing how many event identifiers to remember for de-duplication.
   *
   * @param filters what to subscribe to
   * @param listener receives events, the end-of-backlog signal, and per-relay failures
   * @param deduplicationWindowSize how many recently delivered event ids to remember
   * @return the subscription, which unsubscribes from every relay when closed
   */
  public RelaySubscription subscribe(
      List<EventFilter> filters, SubscriptionListener listener, int deduplicationWindowSize) {
    RelaySubscription subscription =
        new RelaySubscription(
            "sub-" + subscriptionSequence.incrementAndGet(),
            filters,
            listener,
            deduplicationWindowSize);
    subscriptions.add(subscription);
    configuredRelayUris.forEach(
        relayUri -> subscribeQuietly(subscription, connectionsByRelay.get(relayUri), relayUri));
    scheduleBacklogTimeout(subscription);
    return subscription;
  }

  private void subscribeQuietly(
      RelaySubscription subscription, RelayConnection connection, String relayUri) {
    if (connection == null) {
      return;
    }
    try {
      subscription.subscribeOn(connection);
    } catch (IOException e) {
      log.warn("Relay {} refused a subscription: {}", relayUri, e.getMessage());
    }
  }

  /**
   * Give up waiting for relays that never replay their backlog.
   *
   * <p>Without this a single unresponsive relay would withhold the end-of-backlog signal
   * indefinitely, leaving an application showing a loading state forever.
   */
  private void scheduleBacklogTimeout(RelaySubscription subscription) {
    reconnectScheduler.schedule(
        subscription::stopAwaitingBacklog, backlogTimeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  private void resumeSubscriptionsOn(RelayConnection connection) {
    subscriptions.stream()
        .filter(subscription -> subscription.isMissing(connection.getRelayUri()))
        .forEach(
            subscription ->
                subscribeQuietly(subscription, connection, connection.getRelayUri()));
  }

  /**
   * Move relays whose connection has since closed back into the downed set.
   *
   * <p>Without this only startup failures would ever be retried, so a relay that dropped after
   * connecting would be published to forever without reconnecting.
   */
  private void markDroppedRelaysAsUnreachable() {
    connectionsByRelay.forEach(
        (relayUri, connection) -> {
          if (connection.getConnectionState() == ConnectionState.CLOSED) {
            connectionsByRelay.remove(relayUri);
            unreachableRelays.put(relayUri, "Connection closed");
          }
        });
  }

  /**
   * The relays that could not be reached when the pool was built.
   *
   * @return the unreachable relays' URIs
   */
  public List<String> getUnreachableRelays() {
    return configuredRelayUris.stream().filter(unreachableRelays::containsKey).toList();
  }

  @Override
  public void close() {
    reconnectScheduler.shutdownNow();
    subscriptions.forEach(RelaySubscription::close);
    subscriptions.clear();
    connectionsByRelay.values().forEach(this::closeQuietly);
    connectionsByRelay.clear();
  }

  private void closeQuietly(RelayConnection connection) {
    try {
      connection.close();
    } catch (Exception e) {
      log.warn("Failed to close relay {}: {}", connection.getRelayUri(), e.getMessage());
    }
  }
}
