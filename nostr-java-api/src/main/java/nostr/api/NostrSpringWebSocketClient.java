package nostr.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.client.NostrEventDispatcher;
import nostr.api.client.NostrRelayRegistry;
import nostr.api.client.NostrRequestDispatcher;
import nostr.api.client.NostrSubscriptionManager;
import nostr.api.client.WebSocketClientHandlerFactory;
import nostr.api.service.NoteService;
import nostr.api.service.impl.DefaultNoteService;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * Default Nostr client using Spring WebSocket clients to send events and requests to relays.
 */
@Slf4j
public class NostrSpringWebSocketClient implements NostrIF {

  private final NostrRelayRegistry relayRegistry;
  private final NostrEventDispatcher eventDispatcher;
  private final NostrRequestDispatcher requestDispatcher;
  private final NostrSubscriptionManager subscriptionManager;
  private NoteService noteService;

  @Getter private Identity sender;

  public NostrSpringWebSocketClient() {
    this(null, new DefaultNoteService());
  }

  public NostrSpringWebSocketClient() {
    this(null, new DefaultNoteService());
  }

  /**
   * Construct a client with a single relay configured.
   */
  public NostrSpringWebSocketClient(String relayName, String relayUri) {
    this();
    setRelays(Map.of(relayName, relayUri));
  }

  /**
   * Construct a client with a custom note service implementation.
   */
  public NostrSpringWebSocketClient(@NonNull NoteService noteService) {
    this(null, noteService);
  }

  /**
   * Construct a client with a sender identity and a custom note service.
   */
  public NostrSpringWebSocketClient(@NonNull Identity sender, @NonNull NoteService noteService) {
    this.sender = sender;
    this.noteService = noteService;
    this.relayRegistry = new NostrRelayRegistry(buildFactory());
    this.eventDispatcher = new NostrEventDispatcher(this.noteService, this.relayRegistry);
    this.requestDispatcher = new NostrRequestDispatcher(this.relayRegistry);
    this.subscriptionManager = new NostrSubscriptionManager(this.relayRegistry);
  }

  /**
   * Construct a client with a sender identity.
   */
  public NostrSpringWebSocketClient(@NonNull Identity sender) {
    this(sender, new DefaultNoteService());
  }

  /**
   * Get a singleton instance of the client without a preconfigured sender.
   */
  private static final class InstanceHolder {
    private static final NostrSpringWebSocketClient INSTANCE = new NostrSpringWebSocketClient();

    private InstanceHolder() {}
  }

  /**
   * Get a lazily initialized singleton instance of the client without a preconfigured sender.
   */
  public static NostrIF getInstance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Get a lazily initialized singleton instance of the client, configuring the sender if unset.
   */
  public static NostrIF getInstance(@NonNull Identity sender) {
    NostrSpringWebSocketClient instance = InstanceHolder.INSTANCE;
    if (instance.getSender() == null) {
      synchronized (instance) {
        if (instance.getSender() == null) {
          instance.setSender(sender);
        }
      }
    }
    return instance;
  }

  @Override
  public NostrIF setSender(@NonNull Identity sender) {
    this.sender = sender;
    return this;
  }

  @Override
  public NostrIF setRelays(@NonNull Map<String, String> relays) {
    relayRegistry.registerRelays(relays);
    return this;
  }

  @Override
  public List<String> sendEvent(@NonNull IEvent event) {
    return eventDispatcher.send(event);
  }

  @Override
  public List<String> sendEvent(@NonNull IEvent event, Map<String, String> relays) {
    setRelays(relays);
    return sendEvent(event);
  }

  @Override
  public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    return requestDispatcher.sendRequest(filters, subscriptionId);
  }

  @Override
  public List<String> sendRequest(
      @NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
    setRelays(relays);
    return sendRequest(filters, subscriptionId);
  }

  @Override
  public List<String> sendRequest(
      @NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays) {
    setRelays(relays);
    return sendRequest(filtersList, subscriptionId);
  }

  @Override
  public List<String> sendRequest(@NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
    return requestDispatcher.sendRequest(filtersList, subscriptionId);
  }

  public static List<String> sendRequest(
      @NonNull SpringWebSocketClient client,
      @NonNull Filters filters,
      @NonNull String subscriptionId)
      throws IOException {
    return NostrRequestDispatcher.sendRequest(client, filters, subscriptionId);
  }

  @Override
  public AutoCloseable subscribe(
      @NonNull Filters filters,
      @NonNull String subscriptionId,
      @NonNull Consumer<String> listener) {
    return subscribe(filters, subscriptionId, listener, null);
  }

  @Override
  public AutoCloseable subscribe(
      @NonNull Filters filters,
      @NonNull String subscriptionId,
      @NonNull Consumer<String> listener,
      Consumer<Throwable> errorListener) {
    Consumer<Throwable> safeError =
        errorListener != null
            ? errorListener
            : throwable ->
                log.warn(
                    "Subscription error for {} on relays {}",
                    subscriptionId,
                    relayRegistry.getClientMap().keySet(),
                    throwable);

    return subscriptionManager.subscribe(filters, subscriptionId, listener, safeError);
  }

  @Override
  public NostrIF sign(@NonNull Identity identity, @NonNull ISignable signable) {
    identity.sign(signable);
    return this;
  }

  @Override
  public boolean verify(@NonNull GenericEvent event) {
    return eventDispatcher.verify(event);
  }

  @Override
  public Map<String, String> getRelays() {
    return relayRegistry.snapshotRelays();
  }

  public void close() throws IOException {
    relayRegistry.closeAll();
  }

  protected WebSocketClientHandler newWebSocketClientHandler(String relayName, String relayUri)
      throws ExecutionException, InterruptedException {
    return new WebSocketClientHandler(relayName, relayUri);
  }

  private WebSocketClientHandlerFactory buildFactory() {
    return this::newWebSocketClientHandler;
  }
}
