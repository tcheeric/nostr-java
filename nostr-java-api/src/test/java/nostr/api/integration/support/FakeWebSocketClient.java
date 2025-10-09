package nostr.api.integration.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.client.springwebsocket.WebSocketClientIF;
import nostr.event.BaseMessage;

/**
 * Minimal in‑memory WebSocket client used by integration tests to simulate relay behavior.
 *
 * <p>Records sent payloads and allows tests to emit inbound messages or errors to subscribed
 * listeners. Intended for deterministic, fast, and offline test scenarios.
 */
@Slf4j
public class FakeWebSocketClient implements WebSocketClientIF {

  /** The relay URL this fake is bound to (for assertions/identification). */
  @Getter private final String relayUrl;

  private volatile boolean open = true;

  private final List<String> sentPayloads = Collections.synchronizedList(new ArrayList<>());
  private final ConcurrentMap<String, Listener> listeners = new ConcurrentHashMap<>();

  /**
   * Creates a fake client for the given relay URL.
   *
   * @param relayUrl relay endpoint identifier
   */
  public FakeWebSocketClient(@NonNull String relayUrl) {
    this.relayUrl = relayUrl;
  }

  /**
   * Encodes and forwards a message for {@link #send(String)}.
   */
  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  /**
   * Appends the raw JSON to the internal log and returns an OK stub response.
   */
  @Override
  public List<String> send(String json) throws IOException {
    if (!open) {
      throw new IOException("WebSocket session is closed for " + relayUrl);
    }
    sentPayloads.add(json);
    // Return a simple response containing the relay URL for identification
    return List.of("OK:" + relayUrl);
  }

  /**
   * Registers a listener and records the subscription REQ payload.
   */
  @Override
  public AutoCloseable subscribe(
      String requestJson,
      Consumer<String> messageListener,
      Consumer<Throwable> errorListener,
      Runnable closeListener)
      throws IOException {
    Objects.requireNonNull(messageListener, "messageListener");
    Objects.requireNonNull(errorListener, "errorListener");
    if (!open) {
      throw new IOException("WebSocket session is closed for " + relayUrl);
    }
    String id = UUID.randomUUID().toString();
    listeners.put(id, new Listener(messageListener, errorListener, closeListener));
    sentPayloads.add(requestJson);
    return () -> listeners.remove(id);
  }

  /**
   * Closes the fake session and notifies close listeners once.
   */
  @Override
  public void close() throws IOException {
    if (!open) return;
    open = false;
    // Notify close listeners once
    for (Listener listener : listeners.values()) {
      try {
        if (listener.closeListener != null) listener.closeListener.run();
      } catch (Exception e) {
        log.warn("Close listener threw on {}", relayUrl, e);
      }
    }
    listeners.clear();
  }

  /**
   * Returns a snapshot of all sent payloads.
   */
  public List<String> getSentPayloads() {
    return List.copyOf(sentPayloads);
  }

  /**
   * Emits an inbound message to all registered listeners.
   */
  public void emit(String payload) {
    for (Listener listener : listeners.values()) {
      try {
        listener.messageListener.accept(payload);
      } catch (Exception e) {
        if (listener.errorListener != null) listener.errorListener.accept(e);
      }
    }
  }

  /**
   * Emits an inbound error to all registered error listeners.
   */
  public void emitError(Throwable t) {
    for (Listener listener : listeners.values()) {
      if (listener.errorListener != null) listener.errorListener.accept(t);
    }
  }

  private record Listener(
      Consumer<String> messageListener, Consumer<Throwable> errorListener, Runnable closeListener) {}
}
