package nostr.client.springwebsocket;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public class StandardWebSocketClient extends TextWebSocketHandler implements WebSocketClientIF {
  private static final Duration DEFAULT_AWAIT_TIMEOUT = Duration.ofSeconds(60);
  private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(500);

  @Value("${nostr.websocket.await-timeout-ms:60000}")
  private long awaitTimeoutMs;

  @Value("${nostr.websocket.poll-interval-ms:500}")
  private long pollIntervalMs;

  private final WebSocketSession clientSession;
  private List<String> events = new ArrayList<>();
  private final AtomicBoolean completed = new AtomicBoolean(false);

  /**
   * Creates a new {@code StandardWebSocketClient} connected to the provided relay URI.
   *
   * @param relayUri the URI of the relay to connect to
   * @throws java.util.concurrent.ExecutionException   if the WebSocket session fails to
   *     establish
   * @throws InterruptedException if the current thread is interrupted while waiting
   *     for the WebSocket handshake to complete
   */
  public StandardWebSocketClient(@Value("${nostr.relay.uri}") String relayUri)
      throws java.util.concurrent.ExecutionException, InterruptedException {
    this.clientSession =
        new org.springframework.web.socket.client.standard.StandardWebSocketClient()
            .execute(this, new WebSocketHttpHeaders(), URI.create(relayUri))
            .get();
  }

  StandardWebSocketClient(WebSocketSession clientSession, long awaitTimeoutMs, long pollIntervalMs) {
    if (clientSession == null) {
      throw new NullPointerException("clientSession must not be null");
    }
    if (awaitTimeoutMs <= 0) {
      throw new IllegalArgumentException("awaitTimeoutMs must be positive");
    }
    if (pollIntervalMs <= 0) {
      throw new IllegalArgumentException("pollIntervalMs must be positive");
    }
    this.clientSession = clientSession;
    this.awaitTimeoutMs = awaitTimeoutMs;
    this.pollIntervalMs = pollIntervalMs;
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    events.add(message.getPayload());
    completed.setRelease(true);
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    clientSession.sendMessage(new TextMessage(json));
    Duration awaitTimeout = awaitTimeoutMs > 0 ? Duration.ofMillis(awaitTimeoutMs) : DEFAULT_AWAIT_TIMEOUT;
    Duration pollInterval = pollIntervalMs > 0 ? Duration.ofMillis(pollIntervalMs) : DEFAULT_POLL_INTERVAL;
    try {
      await()
          .atMost(awaitTimeout)
          .pollInterval(pollInterval)
          .untilTrue(completed);
    } catch (ConditionTimeoutException e) {
      log.error("Timed out waiting for relay response", e);
      try {
        clientSession.close();
      } catch (IOException closeEx) {
        log.warn("Error closing session after timeout", closeEx);
      }
      events = new ArrayList<>();
      completed.setRelease(false);
      return List.of();
    }
    List<String> eventList = List.copyOf(events);
    events = new ArrayList<>();
    completed.setRelease(false);
    return eventList;
  }

  @Override
  public void close() throws IOException {
    if (clientSession.isOpen()) {
    if (clientSession != null) {
      boolean open = false;
      try {
        open = clientSession.isOpen();
      } catch (Exception e) {
        log.warn("Exception while checking if clientSession is open during close()", e);
      }
      if (open) {
        clientSession.close();
      }
    }
  }

  /**
   * @deprecated use {@link #close()} instead.
   */
  @Deprecated
  public void closeSocket() throws IOException {
    close();
  }
}
