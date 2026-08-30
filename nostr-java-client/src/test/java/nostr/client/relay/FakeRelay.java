package nostr.client.relay;

import nostr.client.springwebsocket.ConnectionState;
import nostr.client.springwebsocket.RelayTimeoutException;
import nostr.event.BaseMessage;
import nostr.event.message.ReqMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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

  /** How the relay answers a {@link #send} call. */
  private enum SendBehaviour {
    ACCEPT,
    REJECT,
    SILENT,
    FAIL
  }

  private final String relayUri;
  private final SendBehaviour sendBehaviour;
  private final String rejectionReason;

  private final List<BaseMessage> sentMessages = new CopyOnWriteArrayList<>();
  private final List<String> sentSubscriptionIds = new CopyOnWriteArrayList<>();
  private final Map<String, Subscriber> subscribers = new ConcurrentHashMap<>();
  private final AtomicLong registrationSequence = new AtomicLong();
  private volatile ConnectionState connectionState = ConnectionState.CONNECTED;

  private record Subscriber(
      String subscriptionId,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener) {}

  private FakeRelay(String relayUri, SendBehaviour sendBehaviour, String rejectionReason) {
    this.relayUri = relayUri;
    this.sendBehaviour = sendBehaviour;
    this.rejectionReason = rejectionReason;
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
    sentMessages.add(message);
    return switch (sendBehaviour) {
      case ACCEPT -> List.of(okResponse(true, ""));
      case REJECT -> List.of(okResponse(false, rejectionReason));
      case SILENT -> throw new RelayTimeoutException(SIMULATED_TIMEOUT_MS);
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
   * Bring a dropped relay back, as a successful reconnect would.
   *
   * <p>Subscribers are not restored: re-subscribing is the caller's responsibility, which is
   * exactly the behaviour tests need to verify.
   */
  public void reconnect() {
    subscribers.clear();
    connectionState = ConnectionState.CONNECTED;
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

  private void requireOpen() throws IOException {
    if (connectionState == ConnectionState.CLOSED) {
      throw new IOException("Relay " + relayUri + " is closed");
    }
  }

  private String okResponse(boolean accepted, String reason) {
    return "[\"OK\",\"event-id\"," + accepted + ",\"" + reason + "\"]";
  }
}
