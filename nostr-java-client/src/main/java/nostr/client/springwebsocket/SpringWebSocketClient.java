package nostr.client.springwebsocket;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringWebSocketClient implements AutoCloseable {
  private final WebSocketClientIF webSocketClientIF;

  @Getter private final String relayUrl;

  public SpringWebSocketClient(
      @NonNull WebSocketClientIF webSocketClientIF, @Value("${nostr.relay.uri}") String relayUrl) {
    this.webSocketClientIF = webSocketClientIF;
    this.relayUrl = relayUrl;
  }

  /**
   * Sends the provided {@link BaseMessage} over the WebSocket connection.
   *
   * @param eventMessage the message to send
   * @return the list of responses from the relay
   * @throws IOException if an I/O error occurs while sending the message
   */
  @NostrRetryable
  public List<String> send(@NonNull BaseMessage eventMessage) throws IOException {
    String json = eventMessage.encode();
    log.debug(
        "Sending {} to relay {} (size={} bytes)",
        eventMessage.getCommand(),
        relayUrl,
        json.length());
    List<String> responses = webSocketClientIF.send(json);
    log.debug(
        "Sent {} to relay {} with {} responses",
        eventMessage.getCommand(),
        relayUrl,
        responses.size());
    return responses;
  }

  @NostrRetryable
  public List<String> send(@NonNull String json) throws IOException {
    log.debug("Sending message to relay {} (size={} bytes)", relayUrl, json.length());
    List<String> responses = webSocketClientIF.send(json);
    log.debug("Sent message to relay {} with {} responses", relayUrl, responses.size());
    return responses;
  }

  @NostrRetryable
  public AutoCloseable subscribe(
      @NonNull BaseMessage requestMessage,
      @NonNull Consumer<String> messageListener,
      @NonNull Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    Objects.requireNonNull(messageListener, "messageListener");
    Objects.requireNonNull(errorListener, "errorListener");
    String json = requestMessage.encode();
    log.debug(
        "Subscribing with {} on relay {} (size={} bytes)",
        requestMessage.getCommand(),
        relayUrl,
        json.length());
    AutoCloseable handle =
        webSocketClientIF.subscribe(json, messageListener, errorListener, closeListener);
    log.debug(
        "Subscription established with {} on relay {}",
        requestMessage.getCommand(),
        relayUrl);
    return handle;
  }

  @NostrRetryable
  public AutoCloseable subscribe(
      @NonNull String json,
      @NonNull Consumer<String> messageListener,
      @NonNull Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    Objects.requireNonNull(messageListener, "messageListener");
    Objects.requireNonNull(errorListener, "errorListener");
    log.debug(
        "Subscribing with raw message to relay {} (size={} bytes)", relayUrl, json.length());
    AutoCloseable handle =
        webSocketClientIF.subscribe(json, messageListener, errorListener, closeListener);
    log.debug("Subscription established on relay {}", relayUrl);
    return handle;
  }

  /**
   * Logs a recovery failure with operation context.
   *
   * @param operation the operation that failed (e.g., "send message", "subscribe")
   * @param size the size of the message in bytes
   * @param ex the exception that caused the failure
   */
  private void logRecoveryFailure(String operation, int size, IOException ex) {
    log.error(
        "Failed to {} to relay {} after retries (size={} bytes)",
        operation,
        relayUrl,
        size,
        ex);
  }

  /**
   * Logs a recovery failure with operation and command context.
   *
   * @param operation the operation that failed (e.g., "send", "subscribe with")
   * @param command the command type from the message
   * @param size the size of the message in bytes
   * @param ex the exception that caused the failure
   */
  private void logRecoveryFailure(String operation, String command, int size, IOException ex) {
    log.error(
        "Failed to {} {} to relay {} after retries (size={} bytes)",
        operation,
        command,
        relayUrl,
        size,
        ex);
  }

  /**
   * This method is invoked by Spring Retry after all retry attempts for the {@link #send(String)}
   * method are exhausted. It logs the failure and rethrows the exception.
   *
   * @param ex the IOException that caused the retries to fail
   * @param json the JSON message that failed to send
   * @return nothing; always throws the exception
   * @throws IOException always thrown to propagate the failure
   */
  @Recover
  public List<String> recover(IOException ex, String json) throws IOException {
    logRecoveryFailure("send message", json.length(), ex);
    throw ex;
  }

  @Recover
  public AutoCloseable recoverSubscription(
      IOException ex,
      String json,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    logRecoveryFailure("subscribe with raw message", json.length(), ex);
    throw ex;
  }

  @Recover
  public AutoCloseable recoverSubscription(
      IOException ex,
      BaseMessage requestMessage,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    String json = requestMessage.encode();
    logRecoveryFailure("subscribe with", requestMessage.getCommand(), json.length(), ex);
    throw ex;
  }

  /**
   * This method is invoked by Spring Retry after all retry attempts for the {@link
   * #send(BaseMessage)} method are exhausted. It logs the failure and rethrows the exception.
   *
   * @param ex the IOException that caused the retries to fail
   * @param eventMessage the BaseMessage that failed to send
   * @return nothing; always throws the exception
   * @throws IOException always thrown to propagate the failure
   */
  @Recover
  public List<String> recover(IOException ex, BaseMessage eventMessage) throws IOException {
    String json = eventMessage.encode();
    logRecoveryFailure("send", eventMessage.getCommand(), json.length(), ex);
    throw ex;
  }

  @Override
  public void close() throws IOException {
    log.debug("Closing WebSocket client for relay {}", relayUrl);
    webSocketClientIF.close();
    log.debug("WebSocket client closed for relay {}", relayUrl);
  }

}
