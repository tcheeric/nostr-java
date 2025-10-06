package nostr.api;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import nostr.api.nip01.NIP01EventBuilder;
import nostr.api.nip01.NIP01MessageFactory;
import nostr.api.nip01.NIP01TagFactory;
import nostr.base.Marker;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.entities.UserProfile;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

/**
 * Facade for NIP-01 (Basic Protocol Flow) - the fundamental building blocks of Nostr.
 *
 * <p>NIP-01 defines the core protocol for creating, signing, and transmitting events over
 * Nostr relays. This class provides a high-level API for working with basic event types,
 * tags, and messages without needing to understand the underlying implementation details.
 *
 * <p><b>What is NIP-01?</b>
 * <ul>
 *   <li><b>Event Structure:</b> Defines the JSON format for events (id, pubkey, created_at,
 *       kind, tags, content, sig)</li>
 *   <li><b>Event Kinds:</b> Basic kinds like text notes (1), metadata (0), contacts (3)</li>
 *   <li><b>Event Types:</b> Regular, replaceable, ephemeral, and addressable events</li>
 *   <li><b>Tags:</b> Standard tags like 'e' (event reference), 'p' (public key), 'd' (identifier)</li>
 *   <li><b>Messages:</b> Client-relay communication (EVENT, REQ, CLOSE, EOSE, NOTICE)</li>
 * </ul>
 *
 * <p><b>Design Pattern:</b> This class uses the Facade Pattern to hide the complexity of:
 * <ul>
 *   <li>{@link NIP01EventBuilder} - Event construction logic</li>
 *   <li>{@link NIP01TagFactory} - Tag creation logic</li>
 *   <li>{@link NIP01MessageFactory} - Message formatting logic</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * // Create NIP01 instance with sender identity
 * Identity identity = new Identity(privateKey);
 * NIP01 nip01 = new NIP01(identity);
 *
 * // Create and send a simple text note
 * nip01.createTextNoteEvent("Hello Nostr!")
 *      .sign()
 *      .send(relayUri);
 *
 * // Create a text note with tags
 * List<BaseTag> tags = List.of(
 *     NIP01.createEventTag("event_id_hex", Marker.REPLY),
 *     NIP01.createPubKeyTag(recipientPublicKey)
 * );
 * nip01.createTextNoteEvent(tags, "Hello @recipient!")
 *      .sign()
 *      .send(relayUri);
 *
 * // Create metadata event
 * UserProfile profile = UserProfile.builder()
 *     .name("Alice")
 *     .about("Nostr enthusiast")
 *     .picture("https://example.com/avatar.jpg")
 *     .build();
 * nip01.createMetadataEvent(profile)
 *      .sign()
 *      .send(relayUri);
 *
 * // Create static tags and messages (without sender)
 * BaseTag eventTag = NIP01.createEventTag("event_id");
 * BaseTag pubKeyTag = NIP01.createPubKeyTag(publicKey);
 * ReqMessage reqMsg = NIP01.createReqMessage("sub_id", List.of(filters));
 * }</pre>
 *
 * <p><b>Event Types Supported:</b>
 * <ul>
 *   <li><b>Text Notes:</b> {@link #createTextNoteEvent(String)} - Basic short-form content (kind 1)</li>
 *   <li><b>Metadata:</b> {@link #createMetadataEvent(UserProfile)} - User profile data (kind 0)</li>
 *   <li><b>Replaceable:</b> {@link #createReplaceableEvent(Integer, String)} - Latest replaces earlier</li>
 *   <li><b>Ephemeral:</b> {@link #createEphemeralEvent(Integer, String)} - Not stored by relays</li>
 *   <li><b>Addressable:</b> {@link #createAddressableEvent(Integer, String)} - Replaceable with identifier</li>
 * </ul>
 *
 * <p><b>Tag Types Supported:</b>
 * <ul>
 *   <li><b>Event tags (e):</b> {@link #createEventTag(String)} - References to other events</li>
 *   <li><b>Public key tags (p):</b> {@link #createPubKeyTag(PublicKey)} - References to users</li>
 *   <li><b>Identifier tags (d):</b> {@link #createIdentifierTag(String)} - For addressable events</li>
 *   <li><b>Address tags (a):</b> {@link #createAddressTag(Integer, PublicKey, String)} - Addressable event refs</li>
 * </ul>
 *
 * <p><b>Message Types Supported:</b>
 * <ul>
 *   <li><b>EVENT:</b> {@link #createEventMessage(GenericEvent, String)} - Publish events</li>
 *   <li><b>REQ:</b> {@link #createReqMessage(String, List)} - Subscribe to events</li>
 *   <li><b>CLOSE:</b> {@link #createCloseMessage(String)} - Unsubscribe</li>
 *   <li><b>EOSE:</b> {@link #createEoseMessage(String)} - End of stored events</li>
 *   <li><b>NOTICE:</b> {@link #createNoticeMessage(String)} - Human-readable messages</li>
 * </ul>
 *
 * <p><b>Method Chaining:</b> This class supports fluent API style:
 * <pre>{@code
 * nip01.createTextNoteEvent("Hello World")  // Create event
 *      .sign()                               // Sign with sender's private key
 *      .send(relayUri)                       // Send to relay
 *      .get();                               // Get response
 * }</pre>
 *
 * <p><b>Sender Management:</b> The sender identity can be set at construction or changed later:
 * <pre>{@code
 * NIP01 nip01 = new NIP01(identity);  // Set sender at construction
 * nip01.setSender(newIdentity);        // Change sender later
 * }</pre>
 *
 * <p><b>Migration Note:</b> Version 0.6.2 deprecated methods that accept Identity parameters
 * in favor of using the configured sender. See {@link #createTextNoteEvent(Identity, String)}.
 *
 * <p><b>Thread Safety:</b> This class is not thread-safe. Each thread should use its own instance.
 *
 * @see NIP01EventBuilder
 * @see NIP01TagFactory
 * @see NIP01MessageFactory
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01 Specification</a>
 * @since 0.1.0
 */
public class NIP01 extends EventNostr {

  private final NIP01EventBuilder eventBuilder;

  public NIP01(Identity sender) {
    super(sender);
    this.eventBuilder = new NIP01EventBuilder(sender);
  }

  @Override
  public NIP01 setSender(@NonNull Identity sender) {
    super.setSender(sender);
    this.eventBuilder.updateDefaultSender(sender);
    return this;
  }

  /**
   * Create a NIP01 text note event without tags.
   *
   * @param content the content of the note
   * @return the text note without tags
   */
  public NIP01 createTextNoteEvent(String content) {
    this.updateEvent(eventBuilder.buildTextNote(content));
    return this;
  }

  /**
   * @deprecated Use {@link #createTextNoteEvent(String)} instead. Sender is now configured at NIP01 construction.
   *             This method will be removed in version 1.0.0.
   */
  @Deprecated(forRemoval = true, since = "0.6.2")
  public NIP01 createTextNoteEvent(Identity sender, String content) {
    this.updateEvent(eventBuilder.buildTextNote(sender, content));
    return this;
  }

  /**
   * Create a NIP01 text note event addressed to specific recipients.
   *
   * @param sender the identity used to sign the event
   * @param content the content of the note
   * @param recipients the list of {@code p} tags identifying recipients' public keys
   * @return this instance for chaining
   */
  public NIP01 createTextNoteEvent(Identity sender, String content, List<PubKeyTag> recipients) {
    this.updateEvent(eventBuilder.buildRecipientTextNote(sender, content, recipients));
    return this;
  }

  /**
   * Create a NIP01 text note event addressed to specific recipients using the configured sender.
   *
   * @param content the content of the note
   * @param recipients the list of {@code p} tags identifying recipients' public keys
   * @return this instance for chaining
   */
  public NIP01 createTextNoteEvent(String content, List<PubKeyTag> recipients) {
    this.updateEvent(eventBuilder.buildRecipientTextNote(content, recipients));
    return this;
  }

  /**
   * Create a NIP01 text note event with recipients.
   *
   * @param tags the tags
   * @param content the content of the note
   * @return a text note event
   */
  public NIP01 createTextNoteEvent(@NonNull List<BaseTag> tags, @NonNull String content) {
    this.updateEvent(eventBuilder.buildTaggedTextNote(tags, content));
    return this;
  }

  public NIP01 createMetadataEvent(@NonNull UserProfile profile) {
    GenericEvent genericEvent =
        Optional.ofNullable(getSender())
            .map(identity -> eventBuilder.buildMetadataEvent(identity, profile.toString()))
            .orElse(eventBuilder.buildMetadataEvent(profile.toString()));
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a replaceable event.
   *
   * @param kind the kind (10000 <= kind < 20000 || kind == 0 || kind == 3)
   * @param content the content
   */
  public NIP01 createReplaceableEvent(Integer kind, String content) {
    this.updateEvent(eventBuilder.buildReplaceableEvent(kind, content));
    return this;
  }

  /**
   * Create a replaceable event.
   *
   * @param tags the note's tags
   * @param kind the kind (10000 <= kind < 20000 || kind == 0 || kind == 3)
   * @param content the note's content
   */
  public NIP01 createReplaceableEvent(List<BaseTag> tags, Integer kind, String content) {
    this.updateEvent(eventBuilder.buildReplaceableEvent(tags, kind, content));
    return this;
  }

  /**
   * Create an ephemeral event.
   *
   * @param kind the kind (20000 <= n < 30000)
   * @param tags the note's tags
   * @param content the note's content
   */
  public NIP01 createEphemeralEvent(List<BaseTag> tags, Integer kind, String content) {
    this.updateEvent(eventBuilder.buildEphemeralEvent(tags, kind, content));
    return this;
  }

  /**
   * Create an ephemeral event.
   *
   * @param kind the kind (20000 <= n < 30000)
   * @param content the note's content
   */
  public NIP01 createEphemeralEvent(Integer kind, String content) {
    this.updateEvent(eventBuilder.buildEphemeralEvent(kind, content));
    return this;
  }

  /**
   * Create an addressable event (A-event as defined by NIP-33).
   *
   * @param kind the event kind (replaceable/addressable kinds per NIP-33)
   * @param content the event's content/comment
   * @return this instance for chaining
   */
  public NIP01 createAddressableEvent(Integer kind, String content) {
    this.updateEvent(eventBuilder.buildAddressableEvent(kind, content));
    return this;
  }

  /**
   * Create an addressable event (A-event as defined by NIP-33).
   *
   * @param tags additional tags to attach to the event (e.g., identifier/address tags)
   * @param kind the event kind (replaceable/addressable kinds per NIP-33)
   * @param content the event's content/comment
   * @return this instance for chaining
   */
  public NIP01 createAddressableEvent(
      @NonNull List<GenericTag> tags, @NonNull Integer kind, String content) {
    this.updateEvent(eventBuilder.buildAddressableEvent(tags, kind, content));
    return this;
  }

  /**
   * Create a NIP01 event tag.
   *
   * @param relatedEventId the related event id
   * @return an event tag with the id of the related event
   */
  public static BaseTag createEventTag(@NonNull String relatedEventId) {
    return NIP01TagFactory.eventTag(relatedEventId);
  }

  /**
   * Create a NIP01 event tag with additional recommended relay and marker.
   *
   * @param idEvent the related event id
   * @param recommendedRelayUrl the recommended relay url
   * @param marker the marker
   * @return an event tag with the id of the related event and optional recommended relay and marker
   */
  public static BaseTag createEventTag(
      @NonNull String idEvent, String recommendedRelayUrl, Marker marker) {
    return NIP01TagFactory.eventTag(idEvent, recommendedRelayUrl, marker);
  }

  /**
   * Create a NIP01 event tag with additional recommended relay and marker.
   *
   * @param idEvent the related event id
   * @param marker the marker
   * @return an event tag with the id of the related event and optional recommended relay and marker
   */
  public static BaseTag createEventTag(@NonNull String idEvent, Marker marker) {
    return NIP01TagFactory.eventTag(idEvent, marker);
  }

  /**
   * Create a NIP01 event tag with additional recommended relay and marker.
   *
   * @param idEvent the related event id
   * @param recommendedRelay the recommended relay
   * @param marker the marker
   * @return an event tag with the id of the related event and optional recommended relay and marker
   */
  public static BaseTag createEventTag(
      @NonNull String idEvent, Relay recommendedRelay, Marker marker) {
    return NIP01TagFactory.eventTag(idEvent, recommendedRelay, marker);
  }

  /**
   * Create a NIP01 pubkey tag.
   *
   * @param publicKey the associated public key
   * @return a pubkey tag with the hex representation of the associated public key
   */
  public static BaseTag createPubKeyTag(@NonNull PublicKey publicKey) {
    return NIP01TagFactory.pubKeyTag(publicKey);
  }

  /**
   * Create a NIP01 pubkey tag with additional recommended relay and petname (as defined in NIP02).
   *
   * @param publicKey the associated public key
   * @param mainRelayUrl the recommended relay
   * @param petName the petname
   * @return a pubkey tag with the hex representation of the associated public key and the optional
   *     recommended relay and petname
   */
  public static BaseTag createPubKeyTag(
      @NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
    return NIP01TagFactory.pubKeyTag(publicKey, mainRelayUrl, petName);
  }

  /**
   * Create a NIP01 pubkey tag with additional recommended relay and petname (as defined in NIP02).
   *
   * @param publicKey the associated public key
   * @param mainRelayUrl the recommended relay
   * @return a pubkey tag with the hex representation of the associated public key and the optional
   *     recommended relay and petname
   */
  public static BaseTag createPubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl) {
    return NIP01TagFactory.pubKeyTag(publicKey, mainRelayUrl);
  }

  /**
   * Create a NIP01 identifier tag ({@code d}-tag).
   *
   * @param id the identifier value for replaceable/addressable events (NIP-33)
   * @return the created identifier tag
   */
  public static BaseTag createIdentifierTag(@NonNull String id) {
    return NIP01TagFactory.identifierTag(id);
  }

  /**
   * Create an address tag ({@code a}-tag) as defined in NIP-33.
   *
   * @param kind the target event kind (e.g., replaceable/addressable kind)
   * @param publicKey the author public key of the addressed event
   * @param idTag an optional {@code d}-tag (identifier) for the addressed event
   * @param relay an optional recommended relay URL for the addressed event
   * @return the created address tag
   */
  public static BaseTag createAddressTag(
      @NonNull Integer kind, @NonNull PublicKey publicKey, BaseTag idTag, Relay relay) {
    return NIP01TagFactory.addressTag(kind, publicKey, idTag, relay);
  }

  public static BaseTag createAddressTag(
      @NonNull Integer kind, @NonNull PublicKey publicKey, String id, Relay relay) {
    return NIP01TagFactory.addressTag(kind, publicKey, id, relay);
  }

  /**
   * Create an address tag ({@code a}-tag) referencing an addressable event (NIP-33).
   *
   * @param kind the event kind
   * @param publicKey the author public key of the addressed event
   * @param id the identifier ({@code d}-tag value)
   * @return the created address tag
   */
  public static BaseTag createAddressTag(
      @NonNull Integer kind, @NonNull PublicKey publicKey, String id) {
    return NIP01TagFactory.addressTag(kind, publicKey, id);
  }

  /**
   * Create an event message to send events requested by clients.
   *
   * @param event the related event
   * @param subscriptionId the related subscription id
   * @return an event message
   */
  public static EventMessage createEventMessage(@NonNull GenericEvent event, String subscriptionId) {
    return NIP01MessageFactory.eventMessage(event, subscriptionId);
  }

  /**
   * Create a REQ message to request events and subscribe to new updates.
   *
   * @param subscriptionId the subscription id
   * @param filtersList the filters list
   * @return a REQ message
   */
  public static ReqMessage createReqMessage(
      @NonNull String subscriptionId, @NonNull List<Filters> filtersList) {
    return NIP01MessageFactory.reqMessage(subscriptionId, filtersList);
  }

  /**
   * Create a CLOSE message to stop previous subscriptions.
   *
   * @param subscriptionId the subscription id
   * @return a CLOSE message
   */
  public static CloseMessage createCloseMessage(@NonNull String subscriptionId) {
    return NIP01MessageFactory.closeMessage(subscriptionId);
  }

  /**
   * Create an EOSE message to indicate the end of stored events and the beginning of events newly
   * received in real-time.
   *
   * @param subscriptionId the subscription id
   * @return an EOSE message
   */
  public static EoseMessage createEoseMessage(@NonNull String subscriptionId) {
    return NIP01MessageFactory.eoseMessage(subscriptionId);
  }

  /**
   * Create a NOTICE message to send human-readable error messages or other things to clients.
   *
   * @param message the human-readable message to send to the client
   * @return a NOTICE message
   */
  public static NoticeMessage createNoticeMessage(@NonNull String message) {
    return NIP01MessageFactory.noticeMessage(message);
  }
}
