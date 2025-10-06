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
import nostr.base.RelayUri;
import nostr.base.SubscriptionId;
import nostr.client.WebSocketClientFactory;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.SpringWebSocketClientFactory;
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
  private final Map<SubscriptionId, SpringWebSocketClient> requestClientMap =
      new ConcurrentHashMap<>();
  private final Function<SubscriptionId, SpringWebSocketClient> requestClientFactory;
  private final WebSocketClientFactory clientFactory;

  @Getter private final String relayName;
  @Getter private final RelayUri relayUri;

  /**
   * Create a handler for a specific relay.
   *
   * @param relayName human-friendly relay name
   * @param relayUri relay WebSocket URI
   */
  protected WebSocketClientHandler(@NonNull String relayName, @NonNull String relayUri)
      throws ExecutionException, InterruptedException {
    this(relayName, new RelayUri(relayUri), new SpringWebSocketClientFactory());
  }

  protected WebSocketClientHandler(
      @NonNull String relayName,
      @NonNull RelayUri relayUri,
      @NonNull WebSocketClientFactory clientFactory)
      throws ExecutionException, InterruptedException {
    this(
        relayName,
        relayUri,
        new SpringWebSocketClient(clientFactory.create(relayUri), relayUri.toString()),
        null,
        null,
        clientFactory);
  }

  WebSocketClientHandler(
      @NonNull String relayName,
      @NonNull RelayUri relayUri,
      @NonNull SpringWebSocketClient eventClient,
      Map<SubscriptionId, SpringWebSocketClient> requestClients,
      Function<SubscriptionId, SpringWebSocketClient> requestClientFactory,
      @NonNull WebSocketClientFactory clientFactory) {
    this.relayName = relayName;
    this.relayUri = relayUri;
    this.eventClient = eventClient;
    this.clientFactory = clientFactory;
    this.requestClientFactory =
        requestClientFactory != null ? requestClientFactory : key -> createRequestClient();
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
  public List<String> sendRequest(
      @NonNull Filters filters, @NonNull SubscriptionId subscriptionId) {
    try {
      @SuppressWarnings("resource")
      SpringWebSocketClient client = getOrCreateRequestClient(subscriptionId);
      return client.send(new ReqMessage(subscriptionId.value(), filters));
    } catch (IOException e) {
      throw new RuntimeException("Failed to send request", e);
    }
  }

  public AutoCloseable subscribe(
      @NonNull Filters filters,
      @NonNull String subscriptionId,
      @NonNull Consumer<String> listener,
      Consumer<Throwable> errorListener) {
    SubscriptionId id = SubscriptionId.of(subscriptionId);
    @SuppressWarnings("resource")
    SpringWebSocketClient client = getOrCreateRequestClient(id);
    Consumer<Throwable> safeError = resolveErrorListener(id, errorListener);
    AutoCloseable delegate = openSubscription(client, filters, id, listener, safeError);

    return new SubscriptionHandle(id, client, delegate, safeError);
  }

  private Consumer<Throwable> resolveErrorListener(
      SubscriptionId subscriptionId, Consumer<Throwable> errorListener) {
    if (errorListener != null) {
      return errorListener;
    }
    return throwable ->
        log.warn(
            "Subscription error on relay {} for {}", relayName, subscriptionId.value(), throwable);
  }

  private AutoCloseable openSubscription(
      SpringWebSocketClient client,
      Filters filters,
      SubscriptionId subscriptionId,
      Consumer<String> listener,
      Consumer<Throwable> errorListener) {
    try {
      return client.subscribe(
          new ReqMessage(subscriptionId.value(), filters),
          listener,
          errorListener,
          () ->
              errorListener.accept(
                  new IOException(
                      "Subscription closed by relay %s for id %s"
                          .formatted(relayName, subscriptionId.value()))));
    } catch (IOException e) {
      throw new RuntimeException("Failed to establish subscription", e);
    }
  }

  private final class SubscriptionHandle implements AutoCloseable {
    private final SubscriptionId subscriptionId;
    private final SpringWebSocketClient client;
    private final AutoCloseable delegate;
    private final Consumer<Throwable> errorListener;

    private SubscriptionHandle(
        SubscriptionId subscriptionId,
        SpringWebSocketClient client,
        AutoCloseable delegate,
        Consumer<Throwable> errorListener) {
      this.subscriptionId = subscriptionId;
      this.client = client;
      this.delegate = delegate;
      this.errorListener = errorListener;
    }

    @Override
    public void close() throws IOException {
      CloseAccumulator accumulator = new CloseAccumulator(errorListener);
      AutoCloseable closeFrameHandle = openCloseFrame(subscriptionId, accumulator);
      closeQuietly(closeFrameHandle, accumulator);
      closeQuietly(delegate, accumulator);

      requestClientMap.remove(subscriptionId);
      closeQuietly(client, accumulator);
      accumulator.rethrowIfNecessary();
    }

    private AutoCloseable openCloseFrame(
        SubscriptionId subscriptionId, CloseAccumulator accumulator) {
      try {
        return client.subscribe(
            new CloseMessage(subscriptionId.value()),
            message -> {},
            errorListener,
            null);
      } catch (IOException e) {
        accumulator.record(e);
        return null;
      }
    }
  }

  private void closeQuietly(AutoCloseable closeable, CloseAccumulator accumulator) {
    if (closeable == null) {
      return;
    }
    try {
      closeable.close();
    } catch (IOException e) {
      accumulator.record(e);
    } catch (Exception e) {
      accumulator.record(e);
    }
  }

  private static final class CloseAccumulator {
    private final Consumer<Throwable> errorListener;
    private IOException ioFailure;
    private Exception nonIoFailure;

    private CloseAccumulator(Consumer<Throwable> errorListener) {
      this.errorListener = errorListener;
    }

    private void record(IOException exception) {
      errorListener.accept(exception);
      if (ioFailure == null) {
        ioFailure = exception;
      }
    }

    private void record(Exception exception) {
      errorListener.accept(exception);
      if (nonIoFailure == null) {
        nonIoFailure = exception;
      }
    }

    private void rethrowIfNecessary() throws IOException {
      if (ioFailure != null) {
        throw ioFailure;
      }
      if (nonIoFailure != null) {
        throw new IOException("Failed to close subscription cleanly", nonIoFailure);
      }
    }
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

  protected SpringWebSocketClient getOrCreateRequestClient(SubscriptionId subscriptionId) {
    try {
      return requestClientMap.computeIfAbsent(subscriptionId, requestClientFactory);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw e;
    }
  }

  private SpringWebSocketClient createRequestClient() {
    try {
      return new SpringWebSocketClient(clientFactory.create(relayUri), relayUri.toString());
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to initialize request client", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while initializing request client", e);
    }
  }
}
