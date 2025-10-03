package nostr.client.springwebsocket;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import nostr.event.BaseMessage;

/**
 * Abstraction of a client-owned WebSocket connection to a Nostr relay.
 *
 * <p>Implementations typically maintain a single active connection and are not required to be
 * thread-safe. Callers should serialize access and invoke {@link #close()} when the client is no
 * longer needed.
 */
public interface WebSocketClientIF extends AutoCloseable {

  /**
   * Sends the provided Nostr message over the current WebSocket connection.
   *
   * <p>The call blocks until the implementation considers the exchange complete (for example, after
   * receiving a response or timing out). The method should be invoked by a single thread at a time
   * as implementations are generally not thread-safe.
   *
   * @param eventMessage the message to encode and transmit
   * @param <T> the specific {@link BaseMessage} subtype
   * @return a list of raw JSON payloads received in response; never {@code null}, but possibly
   *     empty
   * @throws IOException if the message cannot be sent or the connection fails
   */
  <T extends BaseMessage> List<String> send(T eventMessage) throws IOException;

  /**
   * Sends a raw JSON string over the WebSocket connection.
   *
   * <p>Semantics match {@link #send(BaseMessage)}: the call is blocking and should not be invoked
   * concurrently from multiple threads.
   *
   * @param json the JSON payload to transmit
   * @return a list of raw JSON payloads received in response; never {@code null}, but possibly
   *     empty
   * @throws IOException if the message cannot be sent or the connection fails
   */
  List<String> send(String json) throws IOException;

  /**
   * Registers a listener for streaming messages while sending the provided JSON payload
   * asynchronously.
   *
   * <p>The implementation MUST send {@code requestJson} immediately without blocking the caller
   * for relay responses. Inbound messages received on the connection are dispatched to the provided
   * {@code messageListener}. Transport errors should be forwarded to {@code errorListener}, and the
   * optional {@code closeListener} should be invoked exactly once when the underlying connection is
   * closed.
   *
   * @param requestJson the JSON payload to transmit to start the subscription
   * @param messageListener callback invoked for each message received
   * @param errorListener callback invoked when a transport error occurs
   * @param closeListener optional callback invoked when the connection closes normally
   * @return a handle that cancels the subscription when closed
   * @throws IOException if the payload cannot be sent or the connection is unavailable
   */
  AutoCloseable subscribe(
      String requestJson,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException;

  /**
   * Convenience overload that accepts a {@link BaseMessage} and delegates to
   * {@link #subscribe(String, Consumer, Consumer, Runnable)}.
   *
   * @param eventMessage the message to encode and transmit
   * @param messageListener callback invoked for each message received
   * @param errorListener callback invoked when a transport error occurs
   * @param closeListener optional callback invoked when the connection closes normally
   * @return a handle that cancels the subscription when closed
   * @throws IOException if encoding or transmission fails
   */
  default <T extends BaseMessage> AutoCloseable subscribe(
      T eventMessage,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    Objects.requireNonNull(eventMessage, "eventMessage");
    return subscribe(
        eventMessage.encode(),
        Objects.requireNonNull(messageListener, "messageListener"),
        Objects.requireNonNull(errorListener, "errorListener"),
        closeListener);
  }

  /**
   * Closes the underlying WebSocket session and releases associated resources.
   *
   * <p>The caller that created this client is responsible for invoking this method when the
   * connection is no longer required. After invocation, the client should not be used for further
   * send operations.
   *
   * @throws IOException if an I/O error occurs while closing the connection
   */
  @Override
  void close() throws IOException;

  /**
   * @deprecated use {@link #close()} instead.
   */
  @Deprecated
  default void closeSocket() throws IOException {
    close();
  }
}
