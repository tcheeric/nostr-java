package nostr.client.springwebsocket;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
  private final AtomicBoolean completed = new AtomicBoolean(false);
  private final Object sendLock = new Object();
  private List<String> events = new ArrayList<>();
  private volatile boolean awaitingResponse = false;
  private final Map<String, ListenerRegistration> listeners = new ConcurrentHashMap<>();
  private final AtomicBoolean connectionClosed = new AtomicBoolean(false);

  /**
   * Creates a new {@code StandardWebSocketClient} connected to the provided relay URI.
   *
   * @param relayUri the URI of the relay to connect to
   * @throws java.util.concurrent.ExecutionException if the WebSocket session fails to establish
   * @throws InterruptedException if the current thread is interrupted while waiting for the
   *     WebSocket handshake to complete
   */
  public StandardWebSocketClient(@Value("${nostr.relay.uri}") String relayUri)
      throws java.util.concurrent.ExecutionException, InterruptedException {
    this.clientSession =
        new org.springframework.web.socket.client.standard.StandardWebSocketClient()
            .execute(this, new WebSocketHttpHeaders(), URI.create(relayUri))
            .get();
  }

  StandardWebSocketClient(
      WebSocketSession clientSession, long awaitTimeoutMs, long pollIntervalMs) {
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
    dispatchMessage(message.getPayload());
    synchronized (sendLock) {
      if (awaitingResponse) {
        events.add(message.getPayload());
        completed.setRelease(true);
      }
    }
  }

  @Override
  public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
    log.warn("Transport error on WebSocket session", exception);
    notifyError(exception);
    synchronized (sendLock) {
      awaitingResponse = false;
      completed.setRelease(true);
    }
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
      throws Exception {
    super.afterConnectionClosed(session, status);
    if (connectionClosed.compareAndSet(false, true)) {
      notifyClose();
    }
    synchronized (sendLock) {
      awaitingResponse = false;
      completed.setRelease(true);
    }
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    synchronized (sendLock) {
      events = new ArrayList<>();
      awaitingResponse = true;
      completed.setRelease(false);
      clientSession.sendMessage(new TextMessage(json));
    }
    Duration awaitTimeout =
        awaitTimeoutMs > 0 ? Duration.ofMillis(awaitTimeoutMs) : DEFAULT_AWAIT_TIMEOUT;
    Duration pollInterval =
        pollIntervalMs > 0 ? Duration.ofMillis(pollIntervalMs) : DEFAULT_POLL_INTERVAL;
    try {
      await().atMost(awaitTimeout).pollInterval(pollInterval).untilTrue(completed);
    } catch (ConditionTimeoutException e) {
      log.error("Timed out waiting for relay response", e);
      try {
        clientSession.close();
      } catch (IOException closeEx) {
        log.warn("Error closing session after timeout", closeEx);
      }
      synchronized (sendLock) {
        events = new ArrayList<>();
        awaitingResponse = false;
        completed.setRelease(false);
      }
      return List.of();
    }
    synchronized (sendLock) {
      List<String> eventList = List.copyOf(events);
      events = new ArrayList<>();
      awaitingResponse = false;
      completed.setRelease(false);
      return eventList;
    }
  }

  @Override
  public AutoCloseable subscribe(
      String requestJson,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    if (requestJson == null || messageListener == null || errorListener == null) {
      throw new NullPointerException("Subscription parameters must not be null");
    }
    if (!clientSession.isOpen()) {
      throw new IOException("WebSocket session is closed");
    }

    String listenerId = UUID.randomUUID().toString();
    listeners.put(
        listenerId,
        new ListenerRegistration(messageListener, errorListener, closeListener));

    try {
      clientSession.sendMessage(new TextMessage(requestJson));
    } catch (IOException e) {
      listeners.remove(listenerId);
      throw e;
    } catch (RuntimeException e) {
      listeners.remove(listenerId);
      throw new IOException("Failed to send subscription payload", e);
    }

    return () -> listeners.remove(listenerId);
  }

  @Override
  public void close() throws IOException {
    if (clientSession != null) {
      boolean open = false;
      try {
        open = clientSession.isOpen();
      } catch (Exception e) {
        log.warn("Exception while checking if clientSession is open during close()", e);
      }
      if (open) {
        clientSession.close();
        if (connectionClosed.compareAndSet(false, true)) {
          notifyClose();
        }
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

  private void dispatchMessage(String payload) {
    listeners.values().forEach(listener -> safelyInvoke(listener.messageListener(), payload, listener));
  }

  private void notifyError(Throwable throwable) {
    listeners.values().forEach(listener -> safelyInvoke(listener.errorListener(), throwable, listener));
  }

  private void notifyClose() {
    listeners.values().forEach(listener -> safelyInvoke(listener.closeListener(), listener));
    listeners.clear();
  }

  private void safelyInvoke(Consumer<String> consumer, String payload, ListenerRegistration listener) {
    if (consumer == null) {
      return;
    }
    try {
      consumer.accept(payload);
    } catch (Exception e) {
      log.warn("Listener threw exception while handling message", e);
      safelyInvoke(listener.errorListener(), e, listener);
    }
  }

  private void safelyInvoke(Consumer<Throwable> consumer, Throwable throwable, ListenerRegistration ignored) {
    if (consumer == null) {
      return;
    }
    try {
      consumer.accept(throwable);
    } catch (Exception e) {
      log.warn("Listener error callback threw exception", e);
    }
  }

  private void safelyInvoke(Runnable runnable, ListenerRegistration listener) {
    if (runnable == null) {
      return;
    }
    try {
      runnable.run();
    } catch (Exception e) {
      log.warn("Listener close callback threw exception", e);
      safelyInvoke(listener.errorListener(), e, listener);
    }
  }

  private record ListenerRegistration(
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener) {}
}
