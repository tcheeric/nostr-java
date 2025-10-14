package nostr.api.service.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.WebSocketClientHandler;
import nostr.api.service.NoteService;
import nostr.base.IEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Default implementation that dispatches notes through all WebSocket clients. */
@Slf4j
public class DefaultNoteService implements NoteService {

  private final ThreadLocal<Map<String, Throwable>> lastFailures =
      ThreadLocal.withInitial(HashMap::new);
  private final ThreadLocal<Map<String, FailureInfo>> lastFailureDetails =
      ThreadLocal.withInitial(HashMap::new);
  private java.util.function.Consumer<Map<String, Throwable>> failureListener;

  /**
   * Returns a snapshot of relay send failures recorded during the last {@code send} call on the
   * current thread.
   *
   * <p>The map key is the relay name as registered in the client; the value is the exception thrown
   * while attempting to send to that relay. A best effort is made to continue sending to other
   * relays even if one relay fails.
   *
   * @return a copy of the last failure map; empty if the last send had no failures
   */
  public Map<String, Throwable> getLastFailures() {
    return new HashMap<>(lastFailures.get());
  }

  /**
   * Returns structured failure details for the last {@code send} call on this thread.
   *
   * <p>Each entry includes timing, relay name and URI, the thrown exception class/message and the
   * root cause class/message (if any). Use this for richer diagnostics and logging.
   *
   * @return a copy of the last failure details; empty if the last send had no failures
   */
  public Map<String, FailureInfo> getLastFailureDetails() {
    return new HashMap<>(lastFailureDetails.get());
  }

  /**
   * Registers a listener that receives the per‑relay failures map after each {@code send} call.
   *
   * <p>The callback is invoked with a map of relay name to Throwable for relays that failed during
   * the last send attempt. The listener runs on the calling thread and exceptions thrown by the
   * listener are ignored to avoid impacting the main flow.
   *
   * @param listener consumer of the failure map; may be {@code null} to clear
   */
  public void setFailureListener(java.util.function.Consumer<Map<String, Throwable>> listener) {
    this.failureListener = listener;
  }

  @Override
  public List<String> send(
      @NonNull IEvent event, @NonNull Map<String, WebSocketClientHandler> clients) {
    ArrayList<String> responses = new ArrayList<>();
    Map<String, Throwable> failures = new HashMap<>();
    Map<String, FailureInfo> details = new HashMap<>();
    RuntimeException lastFailure = null;

    for (Map.Entry<String, WebSocketClientHandler> entry : clients.entrySet()) {
      String relayName = entry.getKey();
      WebSocketClientHandler client = entry.getValue();
      try {
        responses.addAll(client.sendEvent(event));
      } catch (RuntimeException e) {
        failures.put(relayName, e);
        details.put(relayName, FailureInfo.from(relayName, client.getRelayUri().toString(), e));
        lastFailure = e; // capture and continue to attempt other relays
        log.warn("Failed to send event on relay {}: {}", relayName, e.getMessage());
      }
    }

    lastFailures.set(failures);
    lastFailureDetails.set(details);
    if (failureListener != null && !failures.isEmpty()) {
      try { failureListener.accept(new HashMap<>(failures)); } catch (Exception ignored) {}
    }

    if (responses.isEmpty() && lastFailure != null) {
      throw lastFailure;
    }
    return responses.stream().distinct().toList();
  }

  /**
   * Provides structured information about a relay send failure.
   */
  public static final class FailureInfo {
    public final long timestampEpochMillis;
    public final String relayName;
    public final String relayUri;
    public final String exceptionClass;
    public final String message;
    public final String rootCauseClass;
    public final String rootCauseMessage;

    private FailureInfo(
        long ts,
        String relayName,
        String relayUri,
        String cls,
        String msg,
        String rootCls,
        String rootMsg) {
      this.timestampEpochMillis = ts;
      this.relayName = relayName;
      this.relayUri = relayUri;
      this.exceptionClass = cls;
      this.message = msg;
      this.rootCauseClass = rootCls;
      this.rootCauseMessage = rootMsg;
    }

    private static Throwable root(Throwable t) {
      Throwable r = t;
      while (r.getCause() != null && r.getCause() != r) {
        r = r.getCause();
      }
      return r;
    }

    /**
     * Create a {@link FailureInfo} from a relay identity and a thrown exception.
     *
     * @param relayName human‑readable name configured by the client
     * @param relayUri websocket URI string of the relay
     * @param t the thrown exception
     * @return a populated {@link FailureInfo}
     */
    public static FailureInfo from(String relayName, String relayUri, Throwable t) {
      Throwable r = root(t);
      return new FailureInfo(
          java.time.Instant.now().toEpochMilli(),
          relayName,
          relayUri,
          t.getClass().getName(),
          String.valueOf(t.getMessage()),
          r.getClass().getName(),
          String.valueOf(r.getMessage()));
    }
  }
}
