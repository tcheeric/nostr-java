package nostr.client.relay;

import nostr.client.springwebsocket.ConnectionState;
import nostr.client.springwebsocket.RelayTimeoutException;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * A relay whose behaviour is scripted by the test, standing in for real WebSocket transport.
 *
 * <p>Coordinating several relays means reasoning about relays that disagree: one accepts, one
 * rejects with a reason, one never answers, one drops halfway through a subscription. Those
 * scenarios are impractical to arrange against live relays and awkward to express by mocking a
 * transport session. This fake makes each of them a single call.
 *
 * <p>Scripted behaviour is chosen per instance at construction:
 *
 * <pre>{@code
 * FakeRelay accepting = FakeRelay.accepting("wss://relay.one");
 * FakeRelay banned = FakeRelay.rejecting("wss://relay.two", "blocked: pubkey banned");
 * FakeRelay silent = FakeRelay.silent("wss://relay.three");
 * }</pre>
 *
 * <p>Inbound traffic is driven explicitly with {@link #emit(String)} and {@link #dropConnection()}
 * so tests never depend on timing.
 */
public final class FakeRelay implements RelayConnection {

  /** Reported by a silent relay so tests can assert on the timeout without waiting for one. */
  private static final long SIMULATED_TIMEOUT_MS = 100L;

  /** An event id that is never the one under test, used to prove OKs are matched by id. */
  private static final String SOMEONE_ELSES_EVENT_ID =
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

  /** How the relay answers a {@link #send} call. */
  private enum SendBehaviour {
    ACCEPT,
    REJECT,
    SILENT,
    STALL,
    ACKNOWLEDGE_OTHER_EVENT,
    FAIL
  }

  private final String relayUri;
  private final SendBehaviour sendBehaviour;
  private final String rejectionReason;
  private final CyclicBarrier sendRendezvous;
  private final Runnable duringSend;

  private final List<BaseMessage> sentMessages = new CopyOnWriteArrayList<>();
  private final List<String> sentSubscriptionIds = new CopyOnWriteArrayList<>();
  private final Map<String, Subscriber> subscribers = new ConcurrentHashMap<>();
  private final AtomicLong registrationSequence = new AtomicLong();
  private final CountDownLatch stallLatch = new CountDownLatch(1);
  private final AtomicBoolean requestInFlight = new AtomicBoolean(false);
  private final AtomicInteger peakConcurrentSends = new AtomicInteger();
  private volatile ConnectionState connectionState = ConnectionState.CONNECTED;

  private record Subscriber(
      String subscriptionId,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener) {}

  private FakeRelay(String relayUri, SendBehaviour sendBehaviour, String rejectionReason) {
    this(relayUri, sendBehaviour, rejectionReason, null, null);
  }

  private FakeRelay(
      String relayUri,
      SendBehaviour sendBehaviour,
      String rejectionReason,
      CyclicBarrier sendRendezvous) {
    this(relayUri, sendBehaviour, rejectionReason, sendRendezvous, null);
  }

  private FakeRelay(
      String relayUri,
      SendBehaviour sendBehaviour,
      String rejectionReason,
      CyclicBarrier sendRendezvous,
      Runnable duringSend) {
    this.duringSend = duringSend;
    this.relayUri = relayUri;
    this.sendBehaviour = sendBehaviour;
    this.rejectionReason = rejectionReason;
    this.sendRendezvous = sendRendezvous;
  }

  /**
   * A relay that accepts everything sent to it.
   *
   * @param relayUri the relay URI this fake answers to
   * @return the scripted relay
   */
  public static FakeRelay accepting(String relayUri) {
    return new FakeRelay(relayUri, SendBehaviour.ACCEPT, null);
  }

  /**
   * A relay that rejects everything with a reason, as a relay enforcing a policy would.
   *
   * @param relayUri the relay URI this fake answers to
   * @param reason the verbatim rejection reason, such as {@code "blocked: pubkey banned"}
   * @return the scripted relay
   */
  public static FakeRelay rejecting(String relayUri, String reason) {
    return new FakeRelay(relayUri, SendBehaviour.REJECT, reason);
  }

  /**
   * A relay that accepts the request but never answers, so the caller times out.
   *
   * <p>Reports the timeout the same way real transport does, with a
   * {@link RelayTimeoutException}, so callers can distinguish a slow relay from an unreachable
   * one. The timeout is reported immediately rather than after a real delay, to keep tests fast
   * and free of timing assumptions.
   *
   * @param relayUri the relay URI this fake answers to
   * @return the scripted relay
   */
  public static FakeRelay silent(String relayUri) {
    return new FakeRelay(relayUri, SendBehaviour.SILENT, null);
  }

  /**
   * A relay that cannot be reached at all, failing every send.
   *
   * @param relayUri the relay URI this fake answers to
   * @return the scripted relay
   */
  public static FakeRelay unreachable(String relayUri) {
    return new FakeRelay(relayUri, SendBehaviour.FAIL, null);
  }

  /**
   * A relay whose OK names a different event than the one just sent.
   *
   * <p>A connection can carry answers for several events, so a caller that takes the first OK it
   * sees would credit someone else's acceptance to its own publish. This relay is how that
   * mistake is caught.
   *
   * @param relayUri the relay URI this fake answers to
   * @return the scripted relay
   */
  /**
   * A relay that blocks the caller instead of answering, exercising the pool's own timeout.
   *
   * <p>{@link #silent} reports a timeout immediately, which tests the reporting but never the
   * waiting. A pool that failed to bound its wait would hang forever here, so this is the
   * behaviour that proves the bound exists. Always {@link #release} it, so a failing test cannot
   * leave a thread parked.
   *
   * @param relayUri the relay URI this fake answers to
   * @return the scripted relay
   */
  public static FakeRelay acknowledgingOtherEvents(String relayUri) {
    return new FakeRelay(relayUri, SendBehaviour.ACKNOWLEDGE_OTHER_EVENT, null);
  }

  public static FakeRelay stalling(String relayUri) {
    return new FakeRelay(relayUri, SendBehaviour.STALL, null);
  }

  /**
   * Let any caller blocked by {@link #stalling} proceed.
   *
   * <p>Idempotent, so it is safe in a {@code finally} block whether or not anyone blocked.
   */
  public void release() {
    stallLatch.countDown();
  }

  /**
   * A relay that accepts, but only once every relay sharing the barrier has also been reached.
   *
   * <p>This is how a test distinguishes concurrent fan-out from a sequential loop: a caller that
   * publishes one relay at a time can never satisfy the barrier, so it blocks instead of
   * passing.
   *
   * @param relayUri the relay URI this fake answers to
   * @param sendRendezvous the barrier every participating relay must reach
   * @return the scripted relay
   */
  /**
   * A relay that accepts, running the given action while the send is in progress.
   *
   * <p>The action observes the window in which this relay holds its connection, which is how a
   * test measures whether sends to different relays overlap.
   *
   * @param relayUri the relay URI this fake answers to
   * @param duringSend run while the send is in flight
   * @return the scripted relay
   */
  public static FakeRelay acceptingWhile(String relayUri, Runnable duringSend) {
    return new FakeRelay(relayUri, SendBehaviour.ACCEPT, null, null, duringSend);
  }

  public static FakeRelay acceptingAfter(String relayUri, CyclicBarrier sendRendezvous) {
    return new FakeRelay(relayUri, SendBehaviour.ACCEPT, null, sendRendezvous);
  }

  @Override
  public String getRelayUri() {
    return relayUri;
  }

  @Override
  public ConnectionState getConnectionState() {
    return connectionState;
  }

  @Override
  public <T extends BaseMessage> List<String> send(T message) throws IOException {
    requireOpen();
    requireNoRequestInFlight();
    try {
      return sendWhileHoldingTheConnection(message);
    } finally {
      requestInFlight.set(false);
    }
  }

  /**
   * Reject a second concurrent send, exactly as {@code NostrRelayClient} does.
   *
   * <p>The real client permits one request in flight per connection and throws
   * {@link IllegalStateException} otherwise. A fake that quietly allowed concurrent sends would
   * let a caller look correct in tests and fail against a real relay.
   */
  private void requireNoRequestInFlight() {
    if (!requestInFlight.compareAndSet(false, true)) {
      peakConcurrentSends.accumulateAndGet(2, Math::max);
      throw new IllegalStateException(
          "A request is already in flight. Concurrent send() calls are not supported.");
    }
    peakConcurrentSends.accumulateAndGet(1, Math::max);
  }

  /**
   * How many sends were ever in flight at once, so a test can tell queuing from luck.
   *
   * @return the highest number of overlapping sends observed
   */
  public int getPeakConcurrentSends() {
    return peakConcurrentSends.get();
  }

  private <T extends BaseMessage> List<String> sendWhileHoldingTheConnection(T message)
      throws IOException {
    sentMessages.add(message);
    if (duringSend != null) {
      duringSend.run();
    }
    awaitRendezvous();
    String eventId = message instanceof EventMessage eventMessage
        ? eventMessage.getEvent().getId()
        : "event-id";
    return switch (sendBehaviour) {
      case ACCEPT -> List.of(okResponse(eventId, true, ""));
      case ACKNOWLEDGE_OTHER_EVENT -> List.of(okResponse(SOMEONE_ELSES_EVENT_ID, true, ""));
      case REJECT -> List.of(okResponse(eventId, false, rejectionReason));
      case SILENT -> throw new RelayTimeoutException(SIMULATED_TIMEOUT_MS);
      case STALL -> throw awaitReleaseThenTimeOut();
      case FAIL -> throw new IOException("Cannot reach relay " + relayUri);
    };
  }

  @Override
  public <T extends BaseMessage> AutoCloseable subscribe(
      T requestMessage,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    requireOpen();
    if (sendBehaviour == SendBehaviour.FAIL) {
      throw new IOException("Cannot reach relay " + relayUri);
    }
    String subscriptionId =
        requestMessage instanceof ReqMessage req ? req.getSubscriptionId() : null;
    if (subscriptionId != null) {
      sentSubscriptionIds.add(subscriptionId);
    }
    String registrationId = String.valueOf(registrationSequence.getAndIncrement());
    subscribers.put(
        registrationId,
        new Subscriber(subscriptionId, messageListener, errorListener, closeListener));
    return () -> subscribers.remove(registrationId);
  }

  /**
   * Deliver a payload to every active subscriber, as an inbound relay frame would.
   *
   * @param payload the raw payload to deliver
   */
  public void emit(String payload) {
    subscribers.values().forEach(subscriber -> subscriber.messageListener().accept(payload));
  }

  /**
   * Deliver a payload only to subscribers of one subscription, as a relay routing by
   * subscription identifier would.
   *
   * @param subscriptionId the subscription the payload belongs to
   * @param payload the raw payload to deliver
   */
  public void emitTo(String subscriptionId, String payload) {
    subscribers.values().stream()
        .filter(subscriber -> subscriptionId.equals(subscriber.subscriptionId()))
        .forEach(subscriber -> subscriber.messageListener().accept(payload));
  }

  /**
   * Deliver a sequence of payloads to every active subscriber, in order.
   *
   * <p>Scripting a whole backlog in one call is how tests set up stored-event replay, and how
   * they arrange for the same event to arrive from several relays.
   *
   * @param payloads the raw payloads to deliver, in order
   */
  public void emitAll(List<String> payloads) {
    payloads.forEach(this::emit);
  }

  /**
   * Deliver an end-of-stored-events frame for a subscription.
   *
   * <p>Delaying or withholding this frame is what tests do to simulate a relay that never
   * finishes replaying its backlog: simply never call it for that relay.
   *
   * @param subscriptionId the subscription the frame belongs to
   */
  public void emitEndOfStoredEvents(String subscriptionId) {
    emitTo(subscriptionId, "[\"EOSE\",\"" + subscriptionId + "\"]");
  }

  /**
   * Drop the connection mid-stream, notifying subscribers as a relay disconnect would.
   *
   * <p>This is the failure that silently degrades a long-lived subscription, so tests covering
   * recovery start here.
   */
  public void dropConnection() {
    connectionState = ConnectionState.CLOSED;
    subscribers
        .values()
        .forEach(
            subscriber -> {
              subscriber.errorListener().accept(new IOException("Relay " + relayUri + " dropped"));
              if (subscriber.closeListener() != null) {
                subscriber.closeListener().run();
              }
            });
  }

  /**
   * A fresh connection to the same relay, as reconnecting through a factory would produce.
   *
   * <p>Recovery is modelled as a new connection rather than reviving this one, because that is
   * the only option production has: {@code NostrRelayClient} offers no reopen path, so a closed
   * connection is terminal and callers must obtain a replacement from a
   * {@link RelayConnectionFactory}. Subscriptions do not carry over, which is precisely what
   * makes re-subscription the caller's responsibility to prove.
   *
   * @return a new, connected relay with the same URI and scripted behaviour, and no subscribers
   */
  public FakeRelay reconnected() {
    return new FakeRelay(relayUri, sendBehaviour, rejectionReason, sendRendezvous, duringSend);
  }

  /**
   * Every message this relay was asked to send, in order.
   *
   * @return the messages received, so tests can assert what was published where
   */
  public List<BaseMessage> getSentMessages() {
    return List.copyOf(sentMessages);
  }

  /**
   * The subscription identifiers this relay was asked to open, in order.
   *
   * @return the subscription identifiers, so tests can assert re-subscription happened
   */
  public List<String> getSentSubscriptionIds() {
    return List.copyOf(sentSubscriptionIds);
  }

  /**
   * How many subscribers are currently registered.
   *
   * @return the active subscriber count, so tests can assert handles were released
   */
  public int getActiveSubscriberCount() {
    return subscribers.size();
  }

  @Override
  public void close() {
    connectionState = ConnectionState.CLOSED;
    List<Subscriber> current = new ArrayList<>(subscribers.values());
    subscribers.clear();
    current.stream()
        .map(Subscriber::closeListener)
        .filter(Objects::nonNull)
        .forEach(Runnable::run);
  }

  private void awaitRendezvous() throws IOException {
    if (sendRendezvous == null) {
      return;
    }
    try {
      sendRendezvous.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted awaiting concurrent sends on " + relayUri, e);
    } catch (BrokenBarrierException | TimeoutException e) {
      throw new IOException("Sends to " + relayUri + " were not concurrent", e);
    }
  }

  private RelayTimeoutException awaitReleaseThenTimeOut() throws IOException {
    try {
      stallLatch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while stalling on relay " + relayUri, e);
    }
    return new RelayTimeoutException(SIMULATED_TIMEOUT_MS);
  }

  private void requireOpen() throws IOException {
    if (connectionState == ConnectionState.CLOSED) {
      throw new IOException("Relay " + relayUri + " is closed");
    }
  }

  private String okResponse(String eventId, boolean accepted, String reason) {
    return "[\"OK\",\"" + eventId + "\"," + accepted + ",\"" + reason + "\"]";
  }
}
