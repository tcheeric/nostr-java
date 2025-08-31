package nostr.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.api.service.NoteService;
import nostr.api.service.impl.DefaultNoteService;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import nostr.util.NostrUtil;

/**
 * Default Nostr client using Spring WebSocket clients to send events and requests to relays.
 */
@NoArgsConstructor
public class NostrSpringWebSocketClient implements NostrIF {
  private final Map<String, WebSocketClientHandler> clientMap = new ConcurrentHashMap<>();
  @Getter private Identity sender;

  private NoteService noteService = new DefaultNoteService();

  private static volatile NostrSpringWebSocketClient INSTANCE;

  /**
   * Construct a client with a single relay configured.
   *
   * @param relayName a label for the relay
   * @param relayUri the relay WebSocket URI
   */
  public NostrSpringWebSocketClient(String relayName, String relayUri) {
    setRelays(Map.of(relayName, relayUri));
  }

  /**
   * Construct a client with a custom note service implementation.
   */
  public NostrSpringWebSocketClient(@NonNull NoteService noteService) {
    this.noteService = noteService;
  }

  /**
   * Construct a client with a sender identity and a custom note service.
   */
  public NostrSpringWebSocketClient(@NonNull Identity sender, @NonNull NoteService noteService) {
    this.sender = sender;
    this.noteService = noteService;
  }

  /**
   * Get a singleton instance of the client without a preconfigured sender.
   */
  public static NostrIF getInstance() {
    if (INSTANCE == null) {
      synchronized (NostrSpringWebSocketClient.class) {
        if (INSTANCE == null) {
          INSTANCE = new NostrSpringWebSocketClient();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * Get a singleton instance of the client, initializing the sender if needed.
   */
  public static NostrIF getInstance(@NonNull Identity sender) {
    if (INSTANCE == null) {
      synchronized (NostrSpringWebSocketClient.class) {
        if (INSTANCE == null) {
          INSTANCE = new NostrSpringWebSocketClient(sender);
        } else if (INSTANCE.getSender() == null) {
          INSTANCE.sender = sender; // Initialize sender if not already set
        }
      }
    }
    return INSTANCE;
  }

  /**
   * Construct a client with a sender identity.
   */
  public NostrSpringWebSocketClient(@NonNull Identity sender) {
    this.sender = sender;
  }

  /**
   * Set or replace the sender identity.
   */
  public NostrIF setSender(@NonNull Identity sender) {
    this.sender = sender;
    return this;
  }

  @Override
  /**
   * Configure one or more relays by name and URI; creates client handlers lazily.
   */
  public NostrIF setRelays(@NonNull Map<String, String> relays) {
    relays
        .entrySet()
        .forEach(
            relayEntry -> {
              try {
                clientMap.putIfAbsent(
                    relayEntry.getKey(),
                    new WebSocketClientHandler(relayEntry.getKey(), relayEntry.getValue()));
              } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Failed to initialize WebSocket client handler", e);
              }
            });
    return this;
  }

  @Override
  /**
   * Send an event to all configured relays using the {@link NoteService}.
   */
  public List<String> sendEvent(@NonNull IEvent event) {
    if (event instanceof GenericEvent genericEvent) {
      if (!verify(genericEvent)) {
        throw new IllegalStateException("Event verification failed");
      }
    }

    return noteService.send(event, clientMap);
  }

  @Override
  /**
   * Send an event to the provided relays.
   */
  public List<String> sendEvent(@NonNull IEvent event, Map<String, String> relays) {
    setRelays(relays);
    return sendEvent(event);
  }

  @Override
  /**
   * Send a REQ with a single filter to specific relays.
   */
  public List<String> sendRequest(
      @NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
    return sendRequest(List.of(filters), subscriptionId, relays);
  }

  @Override
  /**
   * Send REQ with multiple filters to specific relays.
   */
  public List<String> sendRequest(
      @NonNull List<Filters> filtersList,
      @NonNull String subscriptionId,
      Map<String, String> relays) {
    setRelays(relays);
    return sendRequest(filtersList, subscriptionId);
  }

  @Override
  /**
   * Send REQ with multiple filters to configured relays; flattens distinct responses.
   */
  public List<String> sendRequest(
      @NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
    return filtersList.stream()
        .map(filters -> sendRequest(filters, subscriptionId))
        .flatMap(List::stream)
        .distinct()
        .toList();
  }

  /**
   * Send a REQ message via the provided client.
   *
   * @param client the WebSocket client to use
   * @param filters the filter
   * @param subscriptionId the subscription identifier
   * @return the relay responses
   * @throws IOException if sending fails
   */
  public static List<String> sendRequest(
      @NonNull SpringWebSocketClient client,
      @NonNull Filters filters,
      @NonNull String subscriptionId)
      throws IOException {
    return client.send(new ReqMessage(subscriptionId, filters));
  }

  @Override
  /**
   * Send a REQ with a single filter to configured relays using a per-subscription client.
   */
  public List<String> sendRequest(@NonNull Filters filters, @NonNull String subscriptionId) {
    createRequestClient(subscriptionId);

    return clientMap.entrySet().stream()
        .filter(entry -> entry.getKey().endsWith(":" + subscriptionId))
        .map(Entry::getValue)
        .map(
            webSocketClientHandler ->
                webSocketClientHandler.sendRequest(filters, webSocketClientHandler.getRelayName()))
        .flatMap(List::stream)
        .toList();
  }

  @Override
  /**
   * Sign a signable object with the provided identity.
   */
  public NostrIF sign(@NonNull Identity identity, @NonNull ISignable signable) {
    identity.sign(signable);
    return this;
  }

  @Override
  /**
   * Verify the Schnorr signature of a GenericEvent.
   */
  public boolean verify(@NonNull GenericEvent event) {
    if (!event.isSigned()) {
      throw new IllegalStateException("The event is not signed");
    }

    var signature = event.getSignature();

    try {
      var message = NostrUtil.sha256(event.get_serializedEvent());
      return Schnorr.verify(message, event.getPubKey().getRawData(), signature.getRawData());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  /**
   * Return a copy of the current relay mapping (name -> URI).
   */
  public Map<String, String> getRelays() {
    return clientMap.values().stream()
        .collect(
            Collectors.toMap(
                WebSocketClientHandler::getRelayName,
                WebSocketClientHandler::getRelayUri,
                (prev, next) -> next,
                HashMap::new));
  }

  /**
   * Close all underlying clients.
   */
  public void close() throws IOException {
    for (WebSocketClientHandler client : clientMap.values()) {
      client.close();
    }
  }

  /**
   * Factory for a new WebSocket client handler; overridable for tests.
   */
  protected WebSocketClientHandler newWebSocketClientHandler(String relayName, String relayUri)
      throws ExecutionException, InterruptedException {
    return new WebSocketClientHandler(relayName, relayUri);
  }

  private void createRequestClient(String subscriptionId) {
    clientMap.entrySet().stream()
        .filter(entry -> !entry.getKey().contains(":"))
        .forEach(
            entry -> {
              String requestKey = entry.getKey() + ":" + subscriptionId;
              clientMap.computeIfAbsent(
                  requestKey,
                  key -> {
                    try {
                      return newWebSocketClientHandler(requestKey, entry.getValue().getRelayUri());
                    } catch (ExecutionException | InterruptedException e) {
                      throw new RuntimeException("Failed to create request client", e);
                    }
                  });
            });
  }
}
