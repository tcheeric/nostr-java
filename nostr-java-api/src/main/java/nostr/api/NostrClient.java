package nostr.api;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.client.relay.NoRelayAcceptedException;
import nostr.client.relay.PublishResult;
import nostr.client.relay.RelayConnection;
import nostr.client.relay.RelayConnectionFactory;
import nostr.client.relay.RelayPool;
import nostr.client.relay.RelaySubscription;
import nostr.client.relay.SubscriptionListener;
import nostr.client.springwebsocket.NostrRelayClient;
import nostr.encryption.DirectMessageService;
import nostr.encryption.Nip17DirectMessageService;
import nostr.event.filter.EventFilter;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.DirectMessageRelayList;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * The entry point for talking to Nostr: an identity, some relays, and the operations between
 * them.
 *
 * <p>The lower modules are deliberately narrow, so an application assembling them itself writes
 * the same connection handling, fan-out, de-duplication and delivery orchestration every time.
 * This client owns that work.
 *
 * <pre>{@code
 * try (NostrClient nostr = NostrClient.builder()
 *         .identity(identity)
 *         .relays("wss://relay.398ja.xyz", "wss://nos.lol")
 *         .build()) {
 *
 *     nostr.publishTextNote("Hello Nostr!");
 *     nostr.sendDirectMessage(recipient, "hi");
 * }
 * }</pre>
 *
 * <p>Nothing here is a replacement for the types below it: events remain {@link GenericEvent},
 * and an application needing finer control can reach the {@link RelayPool} directly.
 *
 * <p><strong>Ownership follows construction.</strong> A pool this client built from relay URIs
 * is closed with it; a pool handed in by the caller is left alone, so it can outlive the client
 * in a container.
 */
public final class NostrClient implements AutoCloseable {

  private final Identity identity;
  private final RelayPool relayPool;
  private final boolean ownsRelayPool;
  private final DirectMessageService directMessages;
  private final RelayListLookup relayLists;
  private final DirectMessagePublisher directMessagePublisher;

  private NostrClient(Builder builder) {
    this.identity = builder.identity;
    this.relayPool = builder.resolveRelayPool();
    this.ownsRelayPool = builder.relayPool == null;
    this.directMessages = new Nip17DirectMessageService(identity);
    this.relayLists = new RelayListLookup(relayPool);
    this.directMessagePublisher =
        new DirectMessagePublisher(directMessages, relayLists, relayPool);
  }

  /**
   * Start describing a client.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sign an event with this client's identity and publish it to every relay.
   *
   * @param event the event to sign and publish
   * @return what each relay did with it
   * @throws NoRelayAcceptedException when not one relay stored the event
   */
  public PublishResult publish(@NonNull GenericEvent event) throws NoRelayAcceptedException {
    return publishAs(identity, event);
  }

  /**
   * Sign an event as somebody else and publish it.
   *
   * <p>An application acting for several keys, such as a bridge or a bot host, would otherwise
   * need one client per key.
   *
   * @param signer the identity to sign with
   * @param event the event to sign and publish
   * @return what each relay did with it
   * @throws NoRelayAcceptedException when not one relay stored the event
   */
  public PublishResult publishAs(@NonNull Identity signer, @NonNull GenericEvent event)
      throws NoRelayAcceptedException {
    signer.sign(event);
    return relayPool.publish(event);
  }

  /**
   * Publish a text note authored by this client's identity.
   *
   * @param content the note's text
   * @return what each relay did with it
   * @throws NoRelayAcceptedException when not one relay stored the note
   */
  public PublishResult publishTextNote(@NonNull String content) throws NoRelayAcceptedException {
    return publish(
        GenericEvent.builder()
            .pubKey(identity.getPublicKey())
            .kind(TEXT_NOTE_KIND)
            .content(content)
            .build());
  }

  /**
   * Subscribe across every relay, receiving each matching event once.
   *
   * @param filters what to subscribe to
   * @param listener receives events, the end-of-backlog signal, and per-relay failures
   * @return the subscription, which unsubscribes everywhere when closed
   */
  public RelaySubscription subscribe(
      @NonNull List<EventFilter> filters, @NonNull SubscriptionListener listener) {
    return relayPool.subscribe(filters, listener);
  }

  /**
   * Send a private direct message to one recipient.
   *
   * @param recipient who to send to
   * @param content the message text
   * @return whether the recipient received it
   */
  public RecipientDeliveryOutcome sendDirectMessage(
      @NonNull PublicKey recipient, @NonNull String content) {
    return sendDirectMessage(List.of(recipient), content).getFirst();
  }

  /**
   * Send a private direct message to several recipients.
   *
   * @param recipients who to send to
   * @param content the message text
   * @return one outcome per recipient, since a group message can partly succeed
   */
  public List<RecipientDeliveryOutcome> sendDirectMessage(
      @NonNull List<PublicKey> recipients, @NonNull String content) {
    return directMessagePublisher.send(
        ChatMessage.builder()
            .from(identity.getPublicKey())
            .to(recipients)
            .content(content)
            .build());
  }

  /**
   * Read an incoming gift wrap back into the message it conceals.
   *
   * @param giftWrap the received wrap
   * @return the message inside
   */
  public ChatMessage readDirectMessage(@NonNull GenericEvent giftWrap) {
    return directMessagePublisher.read(giftWrap);
  }

  /**
   * Find where someone receives private direct messages.
   *
   * @param owner the key whose relay list is wanted
   * @return their relay list, or empty when they published none
   */
  public Optional<DirectMessageRelayList> findDirectMessageRelays(@NonNull PublicKey owner) {
    return relayLists.findFor(owner);
  }

  /**
   * The relay pool underneath, for work this client does not cover.
   *
   * @return the pool
   */
  public RelayPool getRelayPool() {
    return relayPool;
  }

  /**
   * The identity this client signs with by default.
   *
   * @return the identity
   */
  public Identity getIdentity() {
    return identity;
  }

  @Override
  public void close() {
    if (ownsRelayPool) {
      relayPool.close();
    }
  }

  private static final int TEXT_NOTE_KIND = 1;
  private static final long RELAY_CONNECT_TIMEOUT_MS = 60_000L;

  /** Describes a {@link NostrClient} before building it. */
  public static final class Builder {

    private Identity identity;
    private final List<String> relayUris = new ArrayList<>();
    private RelayPool relayPool;
    private RelayConnectionFactory connectionFactory = Builder::connectToRelay;

    private Builder() {}

    /**
     * The identity events are signed with unless a call overrides it.
     *
     * @param identity the signing identity
     * @return this builder
     */
    public Builder identity(@NonNull Identity identity) {
      this.identity = identity;
      return this;
    }

    /**
     * The relays to connect to.
     *
     * @param relayUris the relay WebSocket URIs
     * @return this builder
     */
    public Builder relays(@NonNull String... relayUris) {
      return relays(List.of(relayUris));
    }

    /**
     * The relays to connect to.
     *
     * @param relayUris the relay WebSocket URIs
     * @return this builder
     */
    public Builder relays(@NonNull List<String> relayUris) {
      this.relayUris.addAll(relayUris);
      return this;
    }

    /**
     * Use an existing pool instead of building one.
     *
     * <p>The client will <strong>not</strong> close a pool given this way: whoever created it
     * keeps that responsibility, so the pool may outlive the client.
     *
     * @param relayPool the pool to use
     * @return this builder
     */
    public Builder relayPool(@NonNull RelayPool relayPool) {
      this.relayPool = relayPool;
      return this;
    }

    /**
     * How relay connections are opened, for tests and unusual transports.
     *
     * @param connectionFactory opens a connection for a relay URI
     * @return this builder
     */
    public Builder connectionFactory(@NonNull RelayConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
      return this;
    }

    /**
     * Build the client.
     *
     * @return the client
     */
    public NostrClient build() {
      Objects.requireNonNull(identity, "identity is required");
      if (relayPool == null && relayUris.isEmpty()) {
        throw new IllegalStateException("Either relays or a relay pool is required");
      }
      return new NostrClient(this);
    }

    private RelayPool resolveRelayPool() {
      return relayPool != null ? relayPool : new RelayPool(relayUris, connectionFactory);
    }

    /**
     * Open a real WebSocket connection to a relay.
     *
     * <p>Wrapped rather than referenced directly because the client's constructor reports
     * interruption and connection failure separately, while a caller opening a relay only needs
     * to know whether it is reachable.
     */
    private static RelayConnection connectToRelay(String relayUri) throws IOException {
      try {
        return new NostrRelayClient(relayUri, RELAY_CONNECT_TIMEOUT_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted while connecting to relay " + relayUri, e);
      } catch (ExecutionException e) {
        throw new IOException("Could not connect to relay " + relayUri, e.getCause());
      }
    }
  }
}
