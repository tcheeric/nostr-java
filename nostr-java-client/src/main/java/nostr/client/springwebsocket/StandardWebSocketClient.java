package nostr.client.springwebsocket;

import lombok.NonNull;
import lombok.SneakyThrows;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

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

  private static class SendContext {
    final BlockingQueue<String> events = new LinkedBlockingQueue<>();
    final CountDownLatch latch = new CountDownLatch(1);
  }

  private final ConcurrentLinkedQueue<SendContext> contexts = new ConcurrentLinkedQueue<>();

  @SneakyThrows
  public StandardWebSocketClient(@Value("${nostr.relay.uri}") String relayUri) {
    this.clientSession = new org.springframework.web.socket.client.standard.StandardWebSocketClient()
        .execute(this, new WebSocketHttpHeaders(), URI.create(relayUri)).get();
  }

  // Constructor for testing purposes
  public StandardWebSocketClient(WebSocketSession clientSession) {
    this.clientSession = clientSession;
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
    Duration awaitTimeout = awaitTimeoutMs > 0 ? Duration.ofMillis(awaitTimeoutMs) : DEFAULT_AWAIT_TIMEOUT;
    Duration pollInterval = pollIntervalMs > 0 ? Duration.ofMillis(pollIntervalMs) : DEFAULT_POLL_INTERVAL;
    await()
        .atMost(awaitTimeout)
        .pollInterval(pollInterval)
        .until(() -> context.latch.getCount() == 0);
    List<String> eventList = new ArrayList<>();
    context.events.drainTo(eventList);
    context.events.clear();
    contexts.remove(context);
    return eventList;
  }

  @Override
  public void closeSocket() throws IOException {
    clientSession.close();
  }
}
