package nostr.client.springwebsocket;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * WebSocket client for Nostr relay communication.
 *
 * <p>This client uses {@link CompletableFuture} for response waiting, providing instant
 * notification when responses arrive instead of polling. Send and subscribe operations
 * are retried automatically on transient {@link IOException} failures.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public class NostrRelayClient extends TextWebSocketHandler implements AutoCloseable {
  private static final long DEFAULT_AWAIT_TIMEOUT_MS = 60000L;
  private static final long DEFAULT_MAX_IDLE_TIMEOUT_MS = 3600000L;
  private static final int DEFAULT_MAX_TEXT_MESSAGE_BUFFER_SIZE = 1048576;
  private static final int DEFAULT_MAX_BINARY_MESSAGE_BUFFER_SIZE = 1048576;
  private static final int DEFAULT_MAX_EVENTS_PER_REQUEST = 10_000;
  private static final ThreadFactory RELAY_IO_THREAD_FACTORY =
      Thread.ofVirtual().name("nostr-relay-io-", 0).factory();
  private static final ThreadFactory LISTENER_THREAD_FACTORY =
      Thread.ofVirtual().name("nostr-relay-listener-", 0).factory();
  private static final Executor RELAY_IO_EXECUTOR =
      command -> RELAY_IO_THREAD_FACTORY.newThread(command).start();
  private static final Executor LISTENER_EXECUTOR =
      command -> LISTENER_THREAD_FACTORY.newThread(command).start();

  @Value("${nostr.websocket.await-timeout-ms:60000}")
  private long awaitTimeoutMs;

  @Value("${nostr.websocket.max-idle-timeout-ms:3600000}")
  private long maxIdleTimeoutMs;

  @Value("${nostr.websocket.max-events-per-request:10000}")
  private int maxEventsPerRequest = DEFAULT_MAX_EVENTS_PER_REQUEST;

  private final WebSocketSession clientSession;
  private final ReentrantLock sendLock = new ReentrantLock();
  private PendingRequest pendingRequest;
  private final Map<String, ListenerRegistration> listeners = new ConcurrentHashMap<>();
  private final AtomicReference<ConnectionState> connectionState =
      new AtomicReference<>(ConnectionState.CONNECTING);

  private static final class PendingRequest {
    private final CompletableFuture<List<String>> future = new CompletableFuture<>();
    private final List<String> events = new ArrayList<>();
    private final int maxEvents;

    PendingRequest(int maxEvents) {
      this.maxEvents = maxEvents;
    }

    void addEvent(String event) {
      if (events.size() < maxEvents) {
        events.add(event);
      } else if (events.size() == maxEvents) {
        log.warn("Max events per request limit ({}) reached; subsequent events will be dropped. "
            + "Increase nostr.websocket.max-events-per-request or use narrower filters.", maxEvents);
      }
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

  public NostrRelayClient(@Value("${nostr.relay.uri}") String relayUri)
      throws java.util.concurrent.ExecutionException, InterruptedException {
    this.clientSession = connectSession(relayUri);
    connectionState.set(ConnectionState.CONNECTED);
  }

  public NostrRelayClient(String relayUri, long awaitTimeoutMs)
      throws java.util.concurrent.ExecutionException, InterruptedException {
    if (awaitTimeoutMs <= 0) {
      throw new IllegalArgumentException("awaitTimeoutMs must be positive");
    }
    this.awaitTimeoutMs = awaitTimeoutMs;
    log.info("NostrRelayClient created for {} with awaitTimeoutMs={}", relayUri, awaitTimeoutMs);
    this.clientSession = connectSession(relayUri);
    connectionState.set(ConnectionState.CONNECTED);
  }

  NostrRelayClient(WebSocketSession clientSession, long awaitTimeoutMs) {
    if (clientSession == null) {
      throw new NullPointerException("clientSession must not be null");
    }
    if (awaitTimeoutMs <= 0) {
      throw new IllegalArgumentException("awaitTimeoutMs must be positive");
    }
    this.clientSession = clientSession;
    this.awaitTimeoutMs = awaitTimeoutMs;
    connectionState.set(ConnectionState.CONNECTED);
  }

  /**
   * Connect to a relay asynchronously on a Virtual Thread.
   *
   * @param relayUri relay WebSocket URI
   * @return future that completes with a connected client
   */
  public static CompletableFuture<NostrRelayClient> connectAsync(@NonNull String relayUri) {
    return connectAsync(relayUri, DEFAULT_AWAIT_TIMEOUT_MS);
  }

  /**
   * Connect to a relay asynchronously on a Virtual Thread using a custom send timeout.
   *
   * @param relayUri relay WebSocket URI
   * @param awaitTimeoutMs timeout for blocking send operations
   * @return future that completes with a connected client
   */
  public static CompletableFuture<NostrRelayClient> connectAsync(
      @NonNull String relayUri, long awaitTimeoutMs) {
    Objects.requireNonNull(relayUri, "relayUri");
    if (awaitTimeoutMs <= 0) {
      throw new IllegalArgumentException("awaitTimeoutMs must be positive");
    }
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return new NostrRelayClient(relayUri, awaitTimeoutMs);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CompletionException(
                new IOException("Interrupted while connecting to relay " + relayUri, ex));
          } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new CompletionException(
                new IOException("Failed to connect to relay " + relayUri, cause));
          }
        },
        RELAY_IO_EXECUTOR);
  }

  public ConnectionState getConnectionState() {
    return connectionState.get();
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    log.debug("Relay payload received: {}", message.getPayload());
    dispatchMessage(message.getPayload());
    sendLock.lock();
    try {
      if (pendingRequest != null && !pendingRequest.isDone()) {
        pendingRequest.addEvent(message.getPayload());
        if (isTerminationMessage(message.getPayload())) {
          pendingRequest.complete();
          log.debug("Response future completed with {} events", pendingRequest.eventCount());
        }
      }
    } finally {
      sendLock.unlock();
    }
  }

  private boolean isTerminationMessage(String payload) {
    if (payload == null || payload.length() < 2) {
      return false;
    }
    return payload.startsWith("[\"EOSE\"")
        || payload.startsWith("[\"OK\"")
        || payload.startsWith("[\"NOTICE\"")
        || payload.startsWith("[\"CLOSED\"");
  }

  @Override
  public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
    log.warn("Transport error on WebSocket session", exception);
    notifyError(exception);
    sendLock.lock();
    try {
      if (pendingRequest != null && !pendingRequest.isDone()) {
        pendingRequest.completeExceptionally(exception);
      }
    } finally {
      sendLock.unlock();
    }
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
      throws Exception {
    super.afterConnectionClosed(session, status);
    connectionState.set(ConnectionState.CLOSED);
    notifyClose();
    sendLock.lock();
    try {
      if (pendingRequest != null && !pendingRequest.isDone()) {
        pendingRequest.completeExceptionally(
            new IOException("WebSocket connection closed: " + status));
      }
    } finally {
      sendLock.unlock();
    }
  }

  @NostrRetryable
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    String json = eventMessage.encode();
    log.debug("Sending {} to relay {} (size={} bytes)",
        eventMessage.getCommand(), clientSession.getUri(), json.length());
    return send(json);
  }

  @NostrRetryable
  public List<String> send(String json) throws IOException {
    PendingRequest request;

    sendLock.lock();
    try {
      if (pendingRequest != null && !pendingRequest.isDone()) {
        throw new IllegalStateException(
            "A request is already in flight. Concurrent send() calls are not supported.");
      }
      request = new PendingRequest(maxEventsPerRequest);
      pendingRequest = request;
      log.info("Sending request to relay {}: {}", clientSession.getUri(), json);
      clientSession.sendMessage(new TextMessage(json));
    } finally {
      sendLock.unlock();
    }

    long timeout = awaitTimeoutMs > 0 ? awaitTimeoutMs : DEFAULT_AWAIT_TIMEOUT_MS;
    log.debug("Waiting for relay response with timeout={}ms", timeout);

    try {
      List<String> result = request.getFuture().get(timeout, TimeUnit.MILLISECONDS);
      log.info("Received {} relay events via {}", result.size(), clientSession.getUri());
      return result;
    } catch (TimeoutException e) {
      log.error("Timed out waiting for relay response after {}ms", timeout);
      sendLock.lock();
      try {
        if (pendingRequest == request) {
          pendingRequest = null;
        }
      } finally {
        sendLock.unlock();
      }
      try {
        clientSession.close();
      } catch (IOException closeEx) {
        log.warn("Error closing session after timeout", closeEx);
      }
      throw new RelayTimeoutException(timeout);
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
      sendLock.lock();
      try {
        if (pendingRequest == request) {
          pendingRequest = null;
        }
      } finally {
        sendLock.unlock();
      }
    }
  }

  /**
   * Send an encoded relay message on a Virtual Thread.
   *
   * @param json encoded relay message
   * @return future that completes with relay responses
   */
  public CompletableFuture<List<String>> sendAsync(@NonNull String json) {
    Objects.requireNonNull(json, "json");
    return executeAsyncWithRetry(() -> send(json));
  }

  /**
   * Send a relay message on a Virtual Thread.
   *
   * @param eventMessage relay message object
   * @return future that completes with relay responses
   */
  public <T extends BaseMessage> CompletableFuture<List<String>> sendAsync(@NonNull T eventMessage) {
    Objects.requireNonNull(eventMessage, "eventMessage");
    return executeAsyncWithRetry(() -> send(eventMessage));
  }

  @NostrRetryable
  public <T extends BaseMessage> AutoCloseable subscribe(
      @NonNull T requestMessage,
      @NonNull Consumer<String> messageListener,
      @NonNull Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    String json = requestMessage.encode();
    log.debug("Subscribing with {} on relay {} (size={} bytes)",
        requestMessage.getCommand(), clientSession.getUri(), json.length());
    return subscribe(json, messageListener, errorListener, closeListener);
  }

  @NostrRetryable
  public AutoCloseable subscribe(
      String requestJson,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    Objects.requireNonNull(requestJson, "requestJson");
    Objects.requireNonNull(messageListener, "messageListener");
    Objects.requireNonNull(errorListener, "errorListener");
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

  /**
   * Register a subscription asynchronously on a Virtual Thread.
   *
   * @param requestJson encoded REQ message
   * @param messageListener callback for inbound relay payloads
   * @param errorListener callback for relay transport errors
   * @param closeListener callback when connection closes
   * @return future that completes with the subscription handle
   */
  public CompletableFuture<AutoCloseable> subscribeAsync(
      String requestJson,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener) {
    return executeAsyncWithRetry(
        () -> subscribe(requestJson, messageListener, errorListener, closeListener));
  }

  /**
   * Register a subscription asynchronously on a Virtual Thread.
   *
   * @param requestMessage message object that encodes to a REQ command
   * @param messageListener callback for inbound relay payloads
   * @param errorListener callback for relay transport errors
   * @param closeListener callback when connection closes
   * @return future that completes with the subscription handle
   */
  public <T extends BaseMessage> CompletableFuture<AutoCloseable> subscribeAsync(
      @NonNull T requestMessage,
      @NonNull Consumer<String> messageListener,
      @NonNull Consumer<Throwable> errorListener,
      Runnable closeListener) {
    return executeAsyncWithRetry(
        () -> subscribe(requestMessage, messageListener, errorListener, closeListener));
  }

  @Recover
  public List<String> recover(IOException ex, String json) throws IOException {
    log.error("Failed to send message to relay {} after retries (size={} bytes)",
        clientSession.getUri(), json.length(), ex);
    throw ex;
  }

  @Recover
  public List<String> recover(IOException ex, BaseMessage eventMessage) throws IOException {
    String json = eventMessage.encode();
    log.error("Failed to send {} to relay {} after retries (size={} bytes)",
        eventMessage.getCommand(), clientSession.getUri(), json.length(), ex);
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
    log.error("Failed to subscribe on relay {} after retries (size={} bytes)",
        clientSession.getUri(), json.length(), ex);
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
    log.error("Failed to subscribe with {} on relay {} after retries (size={} bytes)",
        requestMessage.getCommand(), clientSession.getUri(), json.length(), ex);
    throw ex;
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
        connectionState.set(ConnectionState.CLOSED);
        notifyClose();
      }
    }
  }

  private static StandardWebSocketClient createSpringClient() {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    long idleTimeout = getLongProperty("nostr.websocket.max-idle-timeout-ms", DEFAULT_MAX_IDLE_TIMEOUT_MS);
    int textBufferSize = getIntProperty("nostr.websocket.max-text-message-buffer-size", DEFAULT_MAX_TEXT_MESSAGE_BUFFER_SIZE);
    int binaryBufferSize = getIntProperty("nostr.websocket.max-binary-message-buffer-size", DEFAULT_MAX_BINARY_MESSAGE_BUFFER_SIZE);

    container.setDefaultMaxSessionIdleTimeout(idleTimeout);
    container.setDefaultMaxTextMessageBufferSize(textBufferSize);
    container.setDefaultMaxBinaryMessageBufferSize(binaryBufferSize);

    log.info("websocket_container_configured max_idle_timeout_ms={} max_text_buffer={} max_binary_buffer={}",
        idleTimeout, textBufferSize, binaryBufferSize);
    return new StandardWebSocketClient(container);
  }

  private WebSocketSession connectSession(String relayUri)
      throws ExecutionException, InterruptedException {
    return createSpringClient().execute(this, new WebSocketHttpHeaders(), URI.create(relayUri))
        .get();
  }

  private static long getLongProperty(String key, long defaultValue) {
    String value = System.getProperty(key);
    if (value != null && !value.isEmpty()) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        log.warn("Invalid value for property {}: {}, using default: {}", key, value, defaultValue);
      }
    }
    return defaultValue;
  }

  private static int getIntProperty(String key, int defaultValue) {
    String value = System.getProperty(key);
    if (value != null && !value.isEmpty()) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        log.warn("Invalid value for property {}: {}, using default: {}", key, value, defaultValue);
      }
    }
    return defaultValue;
  }

  private void dispatchMessage(String payload) {
    List<ListenerRegistration> activeListeners = List.copyOf(listeners.values());
    activeListeners.forEach(
        listener ->
            LISTENER_EXECUTOR.execute(
                () -> safelyInvoke(listener.messageListener(), payload, listener)));
  }

  private void notifyError(Throwable throwable) {
    List<ListenerRegistration> activeListeners = List.copyOf(listeners.values());
    activeListeners.forEach(
        listener ->
            LISTENER_EXECUTOR.execute(
                () -> safelyInvoke(listener.errorListener(), throwable, listener)));
  }

  private void notifyClose() {
    List<ListenerRegistration> activeListeners = List.copyOf(listeners.values());
    activeListeners.forEach(
        listener ->
            LISTENER_EXECUTOR.execute(() -> safelyInvoke(listener.closeListener(), listener)));
    listeners.clear();
  }

  private void safelyInvoke(Consumer<String> consumer, String payload, ListenerRegistration listener) {
    if (consumer == null) return;
    try {
      consumer.accept(payload);
    } catch (Exception e) {
      log.warn("Listener threw exception while handling message", e);
      safelyInvoke(listener.errorListener(), e, listener);
    }
  }

  private void safelyInvoke(Consumer<Throwable> consumer, Throwable throwable, ListenerRegistration ignored) {
    if (consumer == null) return;
    try {
      consumer.accept(throwable);
    } catch (Exception e) {
      log.warn("Listener error callback threw exception", e);
    }
  }

  private void safelyInvoke(Runnable runnable, ListenerRegistration listener) {
    if (runnable == null) return;
    try {
      runnable.run();
    } catch (Exception e) {
      log.warn("Listener close callback threw exception", e);
      safelyInvoke(listener.errorListener(), e, listener);
    }
  }

  private <T> CompletableFuture<T> executeAsyncWithRetry(IoSupplier<T> operation) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return executeWithRetry(operation);
          } catch (IOException ex) {
            throw new CompletionException(ex);
          }
        },
        RELAY_IO_EXECUTOR);
  }

  private <T> T executeWithRetry(IoSupplier<T> operation) throws IOException {
    long retryDelayMs = NostrRetryable.DELAY;
    IOException lastFailure = null;

    for (int attempt = 1; attempt <= NostrRetryable.MAX_ATTEMPTS; attempt++) {
      try {
        return operation.get();
      } catch (IOException ex) {
        lastFailure = ex;
        if (attempt == NostrRetryable.MAX_ATTEMPTS) {
          break;
        }
        sleepForRetry(retryDelayMs);
        retryDelayMs = Math.max(1L, Math.round(retryDelayMs * NostrRetryable.MULTIPLIER));
      }
    }

    throw lastFailure == null
        ? new IOException("Relay operation failed without exception details")
        : lastFailure;
  }

  private void sleepForRetry(long retryDelayMs) throws IOException {
    try {
      Thread.sleep(retryDelayMs);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting to retry relay operation", ex);
    }
  }

  private record ListenerRegistration(
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener) {}

  @FunctionalInterface
  private interface IoSupplier<T> {
    T get() throws IOException;
  }

}
