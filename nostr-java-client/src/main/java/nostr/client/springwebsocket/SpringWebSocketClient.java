package nostr.client.springwebsocket;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
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
  @SneakyThrows
  public List<String> send(@NonNull BaseMessage eventMessage) {
    String payload = eventMessage.encode();
    log.debug("Sending BaseMessage [{}] to relay {} (size: {} chars)", eventMessage.getCommand(), relayUrl, payload.length());
    List<String> responses = webSocketClientIF.send(payload);
    log.debug("Sent BaseMessage [{}] to relay {} (size: {} chars)", eventMessage.getCommand(), relayUrl, payload.length());
    return responses;
  }

  @NostrRetryable
  public List<String> send(@NonNull String json) throws IOException {
    log.debug("Sending raw message to relay {} (size: {} chars)", relayUrl, json.length());
    List<String> responses = webSocketClientIF.send(json);
    log.debug("Sent raw message to relay {} (size: {} chars)", relayUrl, json.length());
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
    log.error("Failed to send message after retries: {}", json, ex);
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
    log.error("Failed to send message after retries: {}", eventMessage, ex);
    throw ex;
  }

  public void closeSocket() throws IOException {
    log.info("Closing WebSocket client for relay {}", relayUrl);
    webSocketClientIF.closeSocket();
    log.info("WebSocket client closed for relay {}", relayUrl);
  }
}

