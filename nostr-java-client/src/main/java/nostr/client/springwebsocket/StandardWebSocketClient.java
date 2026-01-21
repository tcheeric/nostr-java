package nostr.client.springwebsocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket client for Nostr relay communication.
 *
 * <p>This client uses {@link CompletableFuture} for response waiting, providing instant
 * notification when responses arrive instead of polling. This eliminates race conditions
 * that can occur with polling-based approaches where the response may arrive between
 * poll intervals.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public class StandardWebSocketClient extends TextWebSocketHandler implements WebSocketClientIF {
  private static final long DEFAULT_AWAIT_TIMEOUT_MS = 60000L;
  /** Default max idle timeout for WebSocket sessions (1 hour). Set to 0 for no timeout. */
  private static final long DEFAULT_MAX_IDLE_TIMEOUT_MS = 3600000L;

  @Value("${nostr.websocket.await-timeout-ms:60000}")
  private long awaitTimeoutMs;

  @Value("${nostr.websocket.poll-interval-ms:500}")
  private long pollIntervalMs; // Kept for API compatibility, no longer used for polling

  @Value("${nostr.websocket.max-idle-timeout-ms:3600000}")
  private long maxIdleTimeoutMs;

  private final WebSocketSession clientSession;
  private final Object sendLock = new Object();
  private PendingRequest pendingRequest;
  private final Map<String, ListenerRegistration> listeners = new ConcurrentHashMap<>();
  private final AtomicBoolean connectionClosed = new AtomicBoolean(false);

  /** Encapsulates a pending request's future and its associated events list for thread isolation. */
  private static final class PendingRequest {
    private final CompletableFuture<List<String>> future = new CompletableFuture<>();
    private final List<String> events = new ArrayList<>();

    void addEvent(String event) {
      events.add(event);
    }

    void complete() {
      future.complete(List.copyOf(events));
    }

    void completeExceptionally(Throwable ex) {
      future.completeExceptionally(ex);
    }

    boolean isDone() {
      return future.isDone();
    }

    CompletableFuture<List<String>> getFuture() {
      return future;
    }

    int eventCount() {
      return events.size();
    }
  }

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
    this.clientSession = createSpringClient()
            .execute(this, new WebSocketHttpHeaders(), URI.create(relayUri))
            .get();
  }

  /**
   * Creates a new {@code StandardWebSocketClient} with custom timeout configuration.
   *
   * <p>This constructor allows explicit configuration of timeout values, which is useful
   * when creating clients outside of Spring's dependency injection context or when
   * programmatic timeout configuration is preferred over property-based configuration.
   *
   * @param relayUri the URI of the relay to connect to
   * @param awaitTimeoutMs timeout in milliseconds for awaiting relay responses (must be positive)
   * @param pollIntervalMs polling interval in milliseconds (kept for API compatibility, no longer used)
   * @throws java.util.concurrent.ExecutionException if the WebSocket session fails to establish
   * @throws InterruptedException if the current thread is interrupted while waiting for the
   *     WebSocket handshake to complete
   * @throws IllegalArgumentException if awaitTimeoutMs or pollIntervalMs is not positive
   */
  public StandardWebSocketClient(String relayUri, long awaitTimeoutMs, long pollIntervalMs)
      throws java.util.concurrent.ExecutionException, InterruptedException {
    if (awaitTimeoutMs <= 0) {
      throw new IllegalArgumentException("awaitTimeoutMs must be positive");
    }
    if (pollIntervalMs <= 0) {
      throw new IllegalArgumentException("pollIntervalMs must be positive");
    }
    this.awaitTimeoutMs = awaitTimeoutMs;
    this.pollIntervalMs = pollIntervalMs;
    log.info("StandardWebSocketClient created for {} with awaitTimeoutMs={}, pollIntervalMs={} (event-driven, no polling)",
        relayUri, awaitTimeoutMs, pollIntervalMs);
    this.clientSession = createSpringClient()
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
    log.debug("Relay payload received: {}", message.getPayload());
    dispatchMessage(message.getPayload());
    synchronized (sendLock) {
      if (pendingRequest != null && !pendingRequest.isDone()) {
        pendingRequest.addEvent(message.getPayload());
        // Complete on termination signals: EOSE (end of stored events) or OK (event acceptance)
        if (isTerminationMessage(message.getPayload())) {
          pendingRequest.complete();
          log.debug("Response future completed with {} events", pendingRequest.eventCount());
        }
      }
    }
  }

  /**
   * Checks if the message is a Nostr protocol termination signal.
   *
   * <p>Termination signals indicate the relay has finished sending responses:
   * <ul>
   *   <li>EOSE - End of Stored Events, sent after all matching events for a REQ</li>
   *   <li>OK - Acknowledgment of an EVENT submission</li>
   *   <li>NOTICE - Server notice (often indicates errors)</li>
   *   <li>CLOSED - Subscription closed by relay</li>
   * </ul>
   */
  private boolean isTerminationMessage(String payload) {
    if (payload == null || payload.length() < 2) {
      return false;
    }
    // Quick check for JSON array starting with known termination commands
    // Format: ["EOSE", ...] or ["OK", ...] or ["NOTICE", ...] or ["CLOSED", ...]
    return payload.startsWith("[\"EOSE\"")
        || payload.startsWith("[\"OK\"")
        || payload.startsWith("[\"NOTICE\"")
        || payload.startsWith("[\"CLOSED\"");
  }

  @Override
  public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
    log.warn("Transport error on WebSocket session", exception);
    notifyError(exception);
    synchronized (sendLock) {
      if (pendingRequest != null && !pendingRequest.isDone()) {
        pendingRequest.completeExceptionally(exception);
      }
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
      if (pendingRequest != null && !pendingRequest.isDone()) {
        pendingRequest.completeExceptionally(
            new IOException("WebSocket connection closed: " + status));
      }
    }
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    PendingRequest request;

    synchronized (sendLock) {
      if (pendingRequest != null && !pendingRequest.isDone()) {
        throw new IllegalStateException(
            "A request is already in flight. Concurrent send() calls are not supported. "
                + "Wait for the current request to complete or use separate client instances.");
      }
      request = new PendingRequest();
      pendingRequest = request;
      log.info("Sending request to relay {}: {}", clientSession.getUri(), json);
      clientSession.sendMessage(new TextMessage(json));
    }

    long timeout = awaitTimeoutMs > 0 ? awaitTimeoutMs : DEFAULT_AWAIT_TIMEOUT_MS;
    log.debug("Waiting for relay response with timeout={}ms (event-driven)", timeout);

    try {
      List<String> result = request.getFuture().get(timeout, TimeUnit.MILLISECONDS);
      log.info("Received {} relay events via {}", result.size(), clientSession.getUri());
      return result;
    } catch (TimeoutException e) {
      log.error("Timed out waiting for relay response after {}ms", timeout);
      synchronized (sendLock) {
        if (pendingRequest == request) {
          pendingRequest = null;
        }
      }
      try {
        clientSession.close();
      } catch (IOException closeEx) {
        log.warn("Error closing session after timeout", closeEx);
      }
      return List.of();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting for relay response", e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      throw new IOException("Error waiting for relay response", cause);
    } finally {
      synchronized (sendLock) {
        if (pendingRequest == request) {
          pendingRequest = null;
        }
      }
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
   * Creates a Spring WebSocket client configured with an extended idle timeout.
   *
   * <p>The WebSocketContainer is configured with a max session idle timeout to prevent
   * premature connection closures. This is important for Nostr relays that may have
   * periods of inactivity between messages.
   *
   * @return a configured Spring StandardWebSocketClient
   */
  private static org.springframework.web.socket.client.standard.StandardWebSocketClient createSpringClient() {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    container.setDefaultMaxSessionIdleTimeout(DEFAULT_MAX_IDLE_TIMEOUT_MS);
    log.debug("websocket_container_configured max_idle_timeout_ms={}", DEFAULT_MAX_IDLE_TIMEOUT_MS);
    return new org.springframework.web.socket.client.standard.StandardWebSocketClient(container);
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
