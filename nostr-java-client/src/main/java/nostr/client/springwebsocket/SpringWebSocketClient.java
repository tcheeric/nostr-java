package nostr.client.springwebsocket;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SpringWebSocketClient {
  private final WebSocketClientIF webSocketClientIF;

  @Getter
  private final String relayUrl;

  public SpringWebSocketClient(@NonNull WebSocketClientIF webSocketClientIF,
      @Value("${nostr.relay.uri}") String relayUrl) {
    this.webSocketClientIF = webSocketClientIF;
    this.relayUrl = relayUrl;
  }

  @NostrRetryable
  /**
   * Sends the provided {@link BaseMessage} over the WebSocket connection.
   *
   * @param eventMessage the message to send
   * @return the list of responses from the relay
   * @throws IOException if an I/O error occurs while sending the message
   */
  public List<String> send(@NonNull BaseMessage eventMessage) throws IOException {
    String json = eventMessage.encode();
    log.debug("Sending {} to relay {} (size={} bytes)", eventMessage.getCommand(), relayUrl, json.length());
    List<String> responses = webSocketClientIF.send(json);
    log.debug("Sent {} to relay {} with {} responses", eventMessage.getCommand(), relayUrl, responses.size());
    return responses;
  }

  @NostrRetryable
  public List<String> send(@NonNull String json) throws IOException {
    log.debug("Sending message to relay {} (size={} bytes)", relayUrl, json.length());
    List<String> responses = webSocketClientIF.send(json);
    log.debug("Sent message to relay {} with {} responses", relayUrl, responses.size());
    return responses;
  }

  /**
   * This method is invoked by Spring Retry after all retry attempts for the
   * {@link #send(String)} method are exhausted. It logs the failure and rethrows
   * the exception.
   *
   * @param ex   the IOException that caused the retries to fail
   * @param json the JSON message that failed to send
   * @return nothing; always throws the exception
   * @throws IOException always thrown to propagate the failure
   */
  @Recover
  public List<String> recover(IOException ex, String json) throws IOException {
    log.error("Failed to send message to relay {} after retries (size={} bytes)", relayUrl, json.length(), ex);
    throw ex;
  }

  /**
   * This method is invoked by Spring Retry after all retry attempts for the
   * {@link #send(BaseMessage)} method are exhausted. It logs the failure and
   * rethrows the exception.
   *
   * @param ex           the IOException that caused the retries to fail
   * @param eventMessage the BaseMessage that failed to send
   * @return nothing; always throws the exception
   * @throws IOException always thrown to propagate the failure
   */
  @Recover
  public List<String> recover(IOException ex, BaseMessage eventMessage) throws IOException {
    String json = eventMessage.encode();
    log.error("Failed to send {} to relay {} after retries (size={} bytes)", eventMessage.getCommand(), relayUrl, json.length(), ex);
    throw ex;
  }

  public void closeSocket() throws IOException {
    log.debug("Closing WebSocket client for relay {}", relayUrl);
    webSocketClientIF.closeSocket();
    log.debug("WebSocket client closed for relay {}", relayUrl);
  }
}

