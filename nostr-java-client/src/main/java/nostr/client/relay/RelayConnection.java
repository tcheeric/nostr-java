package nostr.client.relay;

import nostr.client.springwebsocket.ConnectionState;
import nostr.event.BaseMessage;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * A single relay connection, as seen by code that coordinates several relays.
 *
 * <p>This is the seam between relay coordination and relay transport. It exposes only what a
 * caller managing many relays needs — identify, send, subscribe, observe state, close — rather
 * than mirroring the full surface of any one transport implementation. Modules above this
 * interface can therefore be tested against scripted relay behaviour without opening a socket.
 *
 * <p>The interface deliberately names no WebSocket or Spring type of its own. It does reuse
 * {@link ConnectionState}, which already belongs to the transport package because it was public
 * API before this seam existed; moving it would break existing callers for no gain.
 *
 * <p>Implementations are expected to be safe for use from several threads, but they may permit
 * only one request in flight at a time; callers coordinating many relays are responsible for
 * respecting that by serialising their own requests per connection.
 */
public interface RelayConnection extends AutoCloseable {

  /**
   * The URI of the relay this connection talks to.
   *
   * @return the relay WebSocket URI, used to identify the connection in a pool and in logs
   */
  String getRelayUri();

  /**
   * The current state of the underlying connection.
   *
   * @return the connection state, so callers can distinguish a relay that rejected a request
   *     from one that was never reachable
   */
  ConnectionState getConnectionState();

  /**
   * Send a message and wait for the relay's response frames.
   *
   * @param message the message to send
   * @return the raw response payloads the relay returned before completing the request
   * @throws IOException if the message could not be sent or the relay did not respond in time
   */
  <T extends BaseMessage> List<String> send(T message) throws IOException;

  /**
   * Register a long-lived subscription and stream matching payloads to a listener.
   *
   * @param requestMessage the subscription request
   * @param messageListener receives each inbound payload for this subscription
   * @param errorListener receives transport errors affecting this subscription
   * @param closeListener invoked when the underlying connection closes, may be {@code null}
   * @return a handle that cancels the subscription when closed
   * @throws IOException if the subscription could not be registered
   */
  <T extends BaseMessage> AutoCloseable subscribe(
      T requestMessage,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException;

  /**
   * Close the connection and release its resources.
   *
   * @throws IOException if the connection could not be closed cleanly
   */
  @Override
  void close() throws IOException;
}
