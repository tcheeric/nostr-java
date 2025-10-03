package nostr.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.IEvent;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.message.CloseMessage;

/**
 * Internal helper managing a relay connection and per-subscription request clients.
 */
@Slf4j
public class WebSocketClientHandler {
  private final SpringWebSocketClient eventClient;
  private final Map<String, SpringWebSocketClient> requestClientMap = new ConcurrentHashMap<>();
  private final Function<String, SpringWebSocketClient> requestClientFactory;

  @Getter private String relayName;
  @Getter private String relayUri;

  /**
   * Create a handler for a specific relay.
   *
   * @param relayName human-friendly relay name
   * @param relayUri relay WebSocket URI
   */
  protected WebSocketClientHandler(@NonNull String relayName, @NonNull String relayUri)
      throws ExecutionException, InterruptedException {
    this.relayName = relayName;
    this.relayUri = relayUri;
    this.eventClient = new SpringWebSocketClient(new StandardWebSocketClient(relayUri), relayUri);
    this.requestClientFactory = key -> createStandardRequestClient();
  }

  WebSocketClientHandler(
      @NonNull String relayName,
      @NonNull String relayUri,
      @NonNull SpringWebSocketClient eventClient,
      Map<String, SpringWebSocketClient> requestClients,
      Function<String, SpringWebSocketClient> requestClientFactory) {
    this.relayName = relayName;
    this.relayUri = relayUri;
    this.eventClient = eventClient;
    this.requestClientFactory =
        requestClientFactory != null ? requestClientFactory : key -> createStandardRequestClient();
    if (requestClients != null) {
      this.requestClientMap.putAll(requestClients);
    }
  }

  /**
   * Send an event message to the relay using the main client.
   *
   * @param event the event to send
   * @return relay responses (raw JSON messages)
   */
  public List<String> sendEvent(@NonNull IEvent event) {
    ((GenericEvent) event).validate();
    try {
      return eventClient.send(new EventMessage(event)).stream().toList();
    } catch (IOException e) {
      throw new RuntimeException("Failed to send event", e);
    }
  }

  /**
   * Send a REQ message on a per-subscription client associated with this handler.
   *
   * @param filters the filter
   * @param subscriptionId the subscription identifier
   * @return relay responses (raw JSON messages)
   */
  protected List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    try {
      SpringWebSocketClient client = getOrCreateRequestClient(subscriptionId);
      return client.send(new ReqMessage(subscriptionId, filters));
    } catch (IOException e) {
      throw new RuntimeException("Failed to send request", e);
    }
  }

  public AutoCloseable subscribe(
      @NonNull Filters filters,
      @NonNull String subscriptionId,
      @NonNull Consumer<String> listener,
      Consumer<Throwable> errorListener) {
    SpringWebSocketClient client = getOrCreateRequestClient(subscriptionId);
    Consumer<Throwable> safeError =
        errorListener != null
            ? errorListener
            : throwable ->
                log.warn(
                    "Subscription error on relay {} for {}", relayName, subscriptionId, throwable);

    AutoCloseable delegate;
    try {
      delegate =
          client.subscribe(
              new ReqMessage(subscriptionId, filters),
              listener,
              safeError,
              () ->
                  safeError.accept(
                      new IOException(
                          "Subscription closed by relay %s for id %s"
                              .formatted(relayName, subscriptionId))));
    } catch (IOException e) {
      throw new RuntimeException("Failed to establish subscription", e);
    }

    return () -> {
      IOException ioFailure = null;
      Exception nonIoFailure = null;
      AutoCloseable closeFrameHandle = null;
      try {
        closeFrameHandle =
            client.subscribe(
                new CloseMessage(subscriptionId),
                message -> {},
                safeError,
                null);
      } catch (IOException e) {
        safeError.accept(e);
        ioFailure = e;
      } finally {
        if (closeFrameHandle != null) {
          try {
            closeFrameHandle.close();
          } catch (IOException e) {
            safeError.accept(e);
            if (ioFailure == null) {
              ioFailure = e;
            }
          } catch (Exception e) {
            safeError.accept(e);
            if (nonIoFailure == null) {
              nonIoFailure = e;
            }
          }
        }
      }

      try {
        delegate.close();
      } catch (IOException e) {
        safeError.accept(e);
        if (ioFailure == null) {
          ioFailure = e;
        }
      } catch (Exception e) {
        safeError.accept(e);
        if (nonIoFailure == null) {
          nonIoFailure = e;
        }
      }

      requestClientMap.remove(subscriptionId);
      try {
        client.close();
      } catch (IOException e) {
        safeError.accept(e);
        if (ioFailure == null) {
          ioFailure = e;
        }
      }

      if (ioFailure != null) {
        throw ioFailure;
      }
      if (nonIoFailure != null) {
        throw new IOException("Failed to close subscription cleanly", nonIoFailure);
      }
    };
  }

  /**
   * Close the event client and any per-subscription request clients.
   */
  public void close() throws IOException {
    eventClient.close();
    for (SpringWebSocketClient client : requestClientMap.values()) {
      client.close();
    }
  }

  protected SpringWebSocketClient getOrCreateRequestClient(String subscriptionId) {
    try {
      return requestClientMap.computeIfAbsent(subscriptionId, requestClientFactory);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw e;
    }
  }

  private SpringWebSocketClient createStandardRequestClient() {
    try {
      return new SpringWebSocketClient(new StandardWebSocketClient(relayUri), relayUri);
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to initialize request client", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while initializing request client", e);
    }
  }
}
