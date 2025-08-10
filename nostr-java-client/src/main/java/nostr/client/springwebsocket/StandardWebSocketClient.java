package nostr.client.springwebsocket;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
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

/**
 * WebSocket client using Spring's {@code StandardWebSocketClient}.
 * <p>
 * This implementation logs key lifecycle events such as connection
 * establishment, message transmission and reception, and socket closure.
 * Errors occurring during send and close operations are logged at the error
 * level.
 */
@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
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

  @SneakyThrows
  public StandardWebSocketClient(@Value("${nostr.relay.uri}") String relayUri) {
    this.clientSession = new org.springframework.web.socket.client.standard.StandardWebSocketClient()
        .execute(this, new WebSocketHttpHeaders(), URI.create(relayUri)).get();
    log.info("WebSocket connection established with relay {}", relayUri);
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    log.info("Received message: {}", message.getPayload());
    String payload = message.getPayload();
    String truncated = payload.length() > 100 ? payload.substring(0, 100) + "..." : payload;
    String hash = hashPayload(payload);
    log.info("Received message: [truncated] \"{}\" [SHA-256: {}]", truncated, hash);
    events.add(payload);
    completed.setRelease(true);
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    try {
      log.info("Sending message: [hash={}, length={}]", sha256Hex(json), json.length());
      clientSession.sendMessage(new TextMessage(json));
    } catch (IOException e) {
      log.error("Error sending message", e);
      throw e;
    }

    Duration awaitTimeout = awaitTimeoutMs > 0 ? Duration.ofMillis(awaitTimeoutMs) : DEFAULT_AWAIT_TIMEOUT;
    Duration pollInterval = pollIntervalMs > 0 ? Duration.ofMillis(pollIntervalMs) : DEFAULT_POLL_INTERVAL;
    await()
        .atMost(awaitTimeout)
        .pollInterval(pollInterval)
        .untilTrue(completed);
    List<String> eventList = List.copyOf(events);
    events = new ArrayList<>();
    completed.setRelease(false);
    return eventList;
  }

  @Override
  public void closeSocket() throws IOException {
    try {
      clientSession.close();
      log.info("WebSocket connection closed");
    } catch (IOException e) {
      log.error("Error closing WebSocket connection", e);
      throw e;
    }
  }
}
