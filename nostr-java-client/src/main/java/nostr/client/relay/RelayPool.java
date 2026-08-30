package nostr.client.relay;

import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

  private final Map<String, RelayConnection> connectionsByRelay = new LinkedHashMap<>();
  private final Map<String, String> unreachableRelays = new LinkedHashMap<>();
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
    Objects.requireNonNull(relayUris, "relayUris");
    Objects.requireNonNull(connectionFactory, "connectionFactory");
    this.publishTimeout = Objects.requireNonNull(publishTimeout, "publishTimeout");
    relayUris.forEach(relayUri -> connectOrRecordAsDown(relayUri, connectionFactory));
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
      connectionsByRelay.forEach(
          (relayUri, connection) ->
              pending.put(relayUri, executor.submit(sendTo(connection, event))));
      Instant deadline = Instant.now().plus(publishTimeout);
      return pending.entrySet().stream()
          .map(entry -> awaitOutcome(entry.getKey(), entry.getValue(), deadline))
          .toList();
    }
  }

  private Callable<RelayPublishOutcome> sendTo(RelayConnection connection, GenericEvent event) {
    return () -> {
      try {
        return interpretResponses(
            connection.getRelayUri(), event.getId(), connection.send(new EventMessage(event)));
      } catch (RelayTimeoutException e) {
        return RelayPublishOutcome.timedOut(connection.getRelayUri(), e.getMessage());
      } catch (IOException e) {
        return RelayPublishOutcome.unreachable(connection.getRelayUri(), e.getMessage());
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
    return connectionsByRelay.entrySet().stream()
        .filter(entry -> entry.getValue().getConnectionState() == ConnectionState.CONNECTED)
        .map(Map.Entry::getKey)
        .toList();
  }

  /**
   * The relays that could not be reached when the pool was built.
   *
   * @return the unreachable relays' URIs
   */
  public List<String> getUnreachableRelays() {
    return List.copyOf(unreachableRelays.keySet());
  }

  @Override
  public void close() {
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
