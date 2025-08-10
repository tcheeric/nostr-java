package nostr.client.springwebsocket;

import nostr.event.BaseMessage;

import java.io.IOException;
import java.util.List;

/**
 * Abstraction of a client-owned WebSocket connection to a Nostr relay.
 *
 * <p>Implementations typically maintain a single active connection and are
 * not required to be thread-safe. Callers should serialize access and invoke
 * {@link #closeSocket()} when the client is no longer needed.</p>
 */
public interface WebSocketClientIF {

  /**
   * Sends the provided Nostr message over the current WebSocket connection.
   *
   * <p>The call blocks until the implementation considers the exchange
   * complete (for example, after receiving a response or timing out). The
   * method should be invoked by a single thread at a time as implementations
   * are generally not thread-safe.</p>
   *
   * @param eventMessage the message to encode and transmit
   * @param <T>          the specific {@link BaseMessage} subtype
   * @return a list of raw JSON payloads received in response; never
   *         {@code null}, but possibly empty
   * @throws IOException if the message cannot be sent or the connection fails
   */
  <T extends BaseMessage> List<String> send(T eventMessage) throws IOException;

  /**
   * Sends a raw JSON string over the WebSocket connection.
   *
   * <p>Semantics match {@link #send(BaseMessage)}: the call is blocking and
   * should not be invoked concurrently from multiple threads.</p>
   *
   * @param json the JSON payload to transmit
   * @return a list of raw JSON payloads received in response; never
   *         {@code null}, but possibly empty
   * @throws IOException if the message cannot be sent or the connection fails
   */
  List<String> send(String json) throws IOException;

  /**
   * Closes the underlying WebSocket session and releases associated
   * resources.
   *
   * <p>The caller that created this client is responsible for invoking this
   * method when the connection is no longer required. After invocation, the
   * client should not be used for further send operations.</p>
   *
   * @throws IOException if an I/O error occurs while closing the connection
   */
  void closeSocket() throws IOException;
}
