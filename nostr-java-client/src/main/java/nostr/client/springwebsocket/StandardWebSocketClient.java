package nostr.client.springwebsocket;

import lombok.NonNull;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import static org.awaitility.Awaitility.await;
import org.awaitility.core.ConditionTimeoutException;

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
  private final BlockingQueue<SendContext> contexts = new LinkedBlockingQueue<>();

  private static class SendContext {
    final BlockingQueue<String> events = new LinkedBlockingQueue<>();
    final CountDownLatch latch = new CountDownLatch(1);
  }

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
    this.clientSession = clientSession;
    this.awaitTimeoutMs = awaitTimeoutMs;
    this.pollIntervalMs = pollIntervalMs;
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    SendContext context = contexts.poll();
    if (context != null) {
      context.events.offer(message.getPayload());
      context.latch.countDown();
    }
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    SendContext context = new SendContext();
    contexts.offer(context);
    clientSession.sendMessage(new TextMessage(json));
    Duration awaitTimeout =
        awaitTimeoutMs > 0 ? Duration.ofMillis(awaitTimeoutMs) : DEFAULT_AWAIT_TIMEOUT;
    Duration pollInterval =
        pollIntervalMs > 0 ? Duration.ofMillis(pollIntervalMs) : DEFAULT_POLL_INTERVAL;
    try {
      await()
          .atMost(awaitTimeout)
          .pollInterval(pollInterval)
          .until(() -> context.latch.getCount() == 0);
    } catch (ConditionTimeoutException e) {
      contexts.remove(context);
      clientSession.close();
      return Collections.emptyList();
    }
    List<String> eventList = new ArrayList<>();
    context.events.drainTo(eventList);
    context.events.clear();
    return eventList;
  }

  @Override
  public void closeSocket() throws IOException {
    clientSession.close();
  }
}
