package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ISignable;
import nostr.base.ITag;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.annotation.Key;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseEvent;
import nostr.event.BaseTag;
import nostr.event.Deleteable;
import nostr.event.json.deserializer.PublicKeyDeserializer;
import nostr.event.json.deserializer.SignatureDeserializer;
import nostr.event.serializer.EventSerializer;
import nostr.event.util.EventTypeChecker;
import nostr.event.validator.EventValidator;
import nostr.util.NostrException;
import nostr.util.validator.HexStringValidator;

import java.beans.Transient;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic implementation of a Nostr event as defined in NIP-01.
 *
 * <p>This class represents the fundamental building block of the Nostr protocol. Events are
 * immutable records signed with a private key, containing a unique ID, timestamp, kind,
 * tags, and content.
 *
 * <p><b>NIP-01 Event Structure:</b>
 * <pre>{@code
 * {
 *   "id": "event_id_hex",        // SHA-256 hash of canonical serialization
 *   "pubkey": "pubkey_hex",      // Author's public key
 *   "created_at": 1234567890,    // Unix timestamp
 *   "kind": 1,                   // Event kind (see Kind enum)
 *   "tags": [...],               // Array of tags
 *   "content": "...",            // Event content (text, JSON, etc.)
 *   "sig": "signature_hex"       // BIP-340 Schnorr signature
 * }
 * }</pre>
 *
 * <p><b>Event Kinds:</b>
 * <ul>
 *   <li><b>Regular events (kind &lt; 10,000):</b> Immutable, stored indefinitely</li>
 *   <li><b>Replaceable events (10,000-19,999):</b> Latest event replaces earlier ones</li>
 *   <li><b>Ephemeral events (20,000-29,999):</b> Not stored by relays</li>
 *   <li><b>Addressable events (30,000-39,999):</b> Replaceable with 'd' tag identifier</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * // Create and sign an event
 * Identity identity = new Identity(privateKey);
 * GenericEvent event = GenericEvent.builder()
 *     .pubKey(identity.getPublicKey())
 *     .kind(Kind.TEXT_NOTE)
 *     .content("Hello Nostr!")
 *     .tags(List.of(new HashtagTag("nostr")))
 *     .build();
 *
 * event.update(); // Compute ID
 * event.sign(identity.getPrivateKey()); // Sign with private key
 * event.validate(); // Verify all fields are valid
 *
 * // Send to relay
 * client.send(event, relayUri);
 * }</pre>
 *
 * <p><b>Validation:</b> This class uses a Template Method pattern for validation.
 * Subclasses can override {@link #validateKind()}, {@link #validateTags()}, and
 * {@link #validateContent()} to add NIP-specific validation while reusing base validation.
 *
 * <p><b>Serialization:</b> Event serialization is delegated to {@link EventSerializer}
 * which produces canonical NIP-01 JSON format for computing event IDs and signatures.
 *
 * <p><b>Thread Safety:</b> This class is not thread-safe. Create separate instances
 * per thread or use external synchronization.
 *
 * @author squirrel
 * @see EventValidator
 * @see EventSerializer
 * @see EventTypeChecker
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericEvent extends BaseEvent implements ISignable, Deleteable {

  @Key @EqualsAndHashCode.Include private String id;

  @Key
  @JsonProperty("pubkey")
  @EqualsAndHashCode.Include
  @JsonDeserialize(using = PublicKeyDeserializer.class)
  private PublicKey pubKey;

  @Key
  @JsonProperty("created_at")
  @EqualsAndHashCode.Exclude
  private Long createdAt;

  @Key @EqualsAndHashCode.Exclude private Integer kind;

  @Key
  @EqualsAndHashCode.Exclude
  @JsonProperty("tags")
  private List<BaseTag> tags;

  @Key @EqualsAndHashCode.Exclude private String content;

  @Key
  @JsonProperty("sig")
  @EqualsAndHashCode.Exclude
  @JsonDeserialize(using = SignatureDeserializer.class)
  private Signature signature;

  @JsonIgnore @EqualsAndHashCode.Exclude private byte[] _serializedEvent;

  @JsonIgnore @EqualsAndHashCode.Exclude private Integer nip;

  public GenericEvent() {
    this.tags = new ArrayList<>();
  }

  public GenericEvent(@NonNull String id) {
    this();
    setId(id);
  }

  public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind) {
    this(pubKey, kind, new ArrayList<>(), "");
  }

  public GenericEvent(@NonNull PublicKey pubKey, @NonNull Integer kind) {
    this(pubKey, kind, new ArrayList<>(), "");
  }

  public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> tags) {
    this(pubKey, kind, tags, "");
  }

  public GenericEvent(
      @NonNull PublicKey pubKey,
      @NonNull Kind kind,
      @NonNull List<BaseTag> tags,
      @NonNull String content) {
    this(pubKey, kind.getValue(), tags, content);
  }

  public GenericEvent(
      @NonNull PublicKey pubKey,
      @NonNull Integer kind,
      @NonNull List<BaseTag> tags,
      @NonNull String content) {
    this.pubKey = pubKey;
    // Accept provided kind value verbatim for custom kinds (e.g., NIP-defined ranges).
    // Use the Kind-typed constructor when mapping enum constants to values.
    this.kind = kind;
    this.tags = new ArrayList<>(tags);
    this.content = content;

    // Update parents
    updateTagsParents(this.tags);
  }

  public void setId(String id) {
    HexStringValidator.validateHex(id, 64);
    this.id = id;
  }

  @Override
  public String toBech32() {
    if (!isSigned()) {
      this.update();
    }

    try {
      return Bech32.toBech32(Bech32Prefix.NOTE, this.getId());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setTags(List<BaseTag> tags) {
    this.tags = new ArrayList<>(tags);

    for (BaseTag tag : this.tags) {
      tag.setParent(this);
    }
  }

  public List<BaseTag> getTags() {
    return Collections.unmodifiableList(this.tags);
  }

  /**
   * Checks if this event is replaceable per NIP-01.
   *
   * <p>Replaceable events (kind 10,000-19,999) can be superseded by newer events
   * with the same kind from the same author. Relays should only keep the most recent one.
   *
   * @return true if event kind is in replaceable range (10,000-19,999)
   * @see EventTypeChecker#isReplaceable(Integer)
   */
  @Transient
  public boolean isReplaceable() {
    return nostr.event.util.EventTypeChecker.isReplaceable(this.kind);
  }

  /**
   * Checks if this event is ephemeral per NIP-01.
   *
   * <p>Ephemeral events (kind 20,000-29,999) are not stored by relays. They are
   * meant for real-time interactions that don't need persistence.
   *
   * @return true if event kind is in ephemeral range (20,000-29,999)
   * @see EventTypeChecker#isEphemeral(Integer)
   */
  @Transient
  public boolean isEphemeral() {
    return nostr.event.util.EventTypeChecker.isEphemeral(this.kind);
  }

  /**
   * Checks if this event is addressable/parametrized replaceable per NIP-01.
   *
   * <p>Addressable events (kind 30,000-39,999) are replaceable events that include
   * a 'd' tag acting as an identifier. They can be queried and replaced using the
   * combination of author pubkey, kind, and 'd' tag value.
   *
   * @return true if event kind is in addressable range (30,000-39,999)
   * @see EventTypeChecker#isAddressable(Integer)
   */
  @Transient
  public boolean isAddressable() {
    return nostr.event.util.EventTypeChecker.isAddressable(this.kind);
  }

  /**
   * Adds a tag to this event.
   *
   * <p>The tag will be added to the tags list if it's not already present (checked
   * via equals()). The tag's parent will be set to this event.
   *
   * @param tag the tag to add (null tags are ignored)
   */
  public void addTag(BaseTag tag) {
    if (tag == null) {
      return;
    }

    if (tags == null) tags = new ArrayList<>();

    if (!tags.contains(tag)) {
      tag.setParent(this);
      tags.add(tag);
    }
  }

  /**
   * Updates the event's timestamp and computes its ID.
   *
   * <p>This method:
   * <ol>
   *   <li>Sets {@code created_at} to the current Unix timestamp</li>
   *   <li>Serializes the event to canonical NIP-01 JSON format</li>
   *   <li>Computes the event ID as SHA-256 hash of the serialization</li>
   * </ol>
   *
   * <p><b>Important:</b> Call this method before signing the event. The event ID
   * is what gets signed, not the individual fields.
   *
   * <p><b>Thread Safety:</b> This method modifies the event state and is not thread-safe.
   *
   * @throws RuntimeException if serialization fails (wraps NostrException)
   * @see EventSerializer#serializeToBytes
   * @see EventSerializer#computeEventId
   */
  public void update() {
    try {
      this.createdAt = Instant.now().getEpochSecond();
      this._serializedEvent =
          nostr.event.serializer.EventSerializer.serializeToBytes(
              this.pubKey, this.createdAt, this.kind, this.tags, this.content);
      this.id = nostr.event.serializer.EventSerializer.computeEventId(this._serializedEvent);
    } catch (NostrException ex) {
      log.warn("Failed to update event during serialization: {}", ex.getMessage(), ex);
      throw new RuntimeException("Event update failed", ex);
    }
  }

  // Minimal builder to support tests expecting GenericEvent.builder()
  public static GenericEventBuilder builder() {
    return new GenericEventBuilder();
  }

  public static class GenericEventBuilder {
    private String id;
    private PublicKey pubKey;
    private Kind kind;
    private Integer customKind;
    private List<BaseTag> tags = new ArrayList<>();
    private String content = "";
    private Long createdAt;
    private Signature signature;
    private Integer nip;

    public GenericEventBuilder id(String id) { this.id = id; return this; }
    public GenericEventBuilder pubKey(PublicKey pubKey) { this.pubKey = pubKey; return this; }
    public GenericEventBuilder kind(Kind kind) { this.kind = kind; return this; }
    public GenericEventBuilder customKind(Integer customKind) { this.customKind = customKind; return this; }
    public GenericEventBuilder tags(List<BaseTag> tags) { this.tags = tags; return this; }
    public GenericEventBuilder content(String content) { this.content = content; return this; }
    public GenericEventBuilder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
    public GenericEventBuilder signature(Signature signature) { this.signature = signature; return this; }
    public GenericEventBuilder nip(Integer nip) { this.nip = nip; return this; }

    public GenericEvent build() {
      GenericEvent event = new GenericEvent();
      if (id != null) event.setId(id);
      event.setPubKey(pubKey);

      if (customKind == null && kind == null) {
        throw new IllegalArgumentException("A kind value must be provided when building a GenericEvent.");
      }
      event.setKind(customKind != null ? customKind : kind.getValue());

      event.setTags(tags != null ? new ArrayList<>(tags) : new ArrayList<>());
      event.setContent(content != null ? content : "");
      event.setCreatedAt(createdAt);
      event.setSignature(signature);
      event.setNip(nip);
      return event;
    }
  }

  /** Compatibility accessors for previously named serializedEventCache */
  @com.fasterxml.jackson.annotation.JsonIgnore
  public byte[] getSerializedEventCache() {
    return this.get_serializedEvent();
  }

  @com.fasterxml.jackson.annotation.JsonIgnore
  public void setSerializedEventCache(byte[] bytes) {
    this.set_serializedEvent(bytes);
  }

  @Transient
  public boolean isSigned() {
    return this.signature != null;
  }

  /**
   * Validates all event fields according to NIP-01 specification.
   *
   * <p>This method uses the Template Method pattern. It validates base fields that
   * all events must have, then calls protected methods that subclasses can override
   * to add NIP-specific validation.
   *
   * <p><b>Validation Steps:</b>
   * <ol>
   *   <li>Validates event ID (64-character hex string)</li>
   *   <li>Validates public key (64-character hex string)</li>
   *   <li>Validates signature (128-character hex string)</li>
   *   <li>Validates created_at (non-negative Unix timestamp)</li>
   *   <li>Calls {@link #validateKind()} (can be overridden)</li>
   *   <li>Calls {@link #validateTags()} (can be overridden)</li>
   *   <li>Calls {@link #validateContent()} (can be overridden)</li>
   * </ol>
   *
   * <p><b>Usage Example:</b>
   * <pre>{@code
   * GenericEvent event = createAndSignEvent();
   * try {
   *     event.validate();
   *     // Event is valid, safe to send to relay
   * } catch (AssertionError e) {
   *     // Event is invalid, fix before sending
   *     log.error("Invalid event: {}", e.getMessage());
   * }
   * }</pre>
   *
   * @throws AssertionError if any field fails validation
   * @throws NullPointerException if required fields are null
   * @see EventValidator
   */
  public void validate() {
    // Validate base fields
    EventValidator.validateId(this.id);
    EventValidator.validatePubKey(this.pubKey);
    EventValidator.validateSignature(this.signature);
    EventValidator.validateCreatedAt(this.createdAt);

    // Call protected methods that can be overridden by subclasses
    validateKind();
    validateTags();
    validateContent();
  }

  /**
   * Validates the event kind.
   *
   * <p>Subclasses can override this method to add kind-specific validation.
   * The default implementation validates that kind is non-negative.
   *
   * @throws AssertionError if kind is invalid
   */
  protected void validateKind() {
    EventValidator.validateKind(this.kind);
  }

  /**
   * Validates the event tags.
   *
   * <p>Subclasses can override this method to add NIP-specific tag validation.
   * For example, ZapRequestEvent requires 'amount' and 'relays' tags.
   *
   * <p><b>Example Override:</b>
   * <pre>{@code
   * @Override
   * protected void validateTags() {
   *     super.validateTags(); // Call base validation first
   *     requireTag("amount");  // NIP-specific requirement
   * }
   * }</pre>
   *
   * @throws AssertionError if tags are invalid
   */
  protected void validateTags() {
    EventValidator.validateTags(this.tags);
  }

  /**
   * Validates the event content.
   *
   * <p>Subclasses can override this method to add content-specific validation.
   * The default implementation validates that content is non-null.
   *
   * @throws AssertionError if content is invalid
   */
  protected void validateContent() {
    EventValidator.validateContent(this.content);
  }

  private String serialize() throws NostrException {
    return nostr.event.serializer.EventSerializer.serialize(
        this.pubKey, this.createdAt, this.kind, this.tags, this.content);
  }

  @Transient
  @Override
  public Consumer<Signature> getSignatureConsumer() {
    return this::setSignature;
  }

  @Transient
  @Override
  public Supplier<ByteBuffer> getByteArraySupplier() {
    this.update();
    if (log.isTraceEnabled()) {
      log.trace("Serialized event: {}", new String(this.get_serializedEvent()));
    }
    return () -> ByteBuffer.wrap(this.get_serializedEvent());
  }

  protected final void updateTagsParents(List<? extends BaseTag> tagList) {
    if (tagList != null && !tagList.isEmpty()) {
      for (ITag t : tagList) {
        t.setParent(this);
      }
    }
  }

  protected <T extends BaseTag> void addStandardTag(List<T> tag) {
    Optional.ofNullable(tag).ifPresent(tagList -> tagList.forEach(this::addStandardTag));
  }

  protected void addStandardTag(BaseTag tag) {
    Optional.ofNullable(tag).ifPresent(this::addTag);
  }

  protected void addGenericTag(String key, Integer nip, Object value) {
    Optional.ofNullable(value).ifPresent(s -> addTag(BaseTag.create(key, s.toString())));
  }

  protected void addStringListTag(String label, Integer nip, List<String> tag) {
    Optional.ofNullable(tag).ifPresent(tagList -> BaseTag.create(label, tagList));
  }

  protected BaseTag getTag(@NonNull String code) {
    return getTags().stream().filter(tag -> code.equals(tag.getCode())).findFirst().orElseThrow();
  }

  protected List<BaseTag> getTags(@NonNull String code) {
    return getTags().stream().filter(tag -> code.equals(tag.getCode())).toList();
  }

  /**
   * Ensure that a tag with the provided code exists.
   *
   * @param code the tag code to search for
   * @return the first matching tag
   * @throws AssertionError if no tag with the given code is present
   */
  protected BaseTag requireTag(@NonNull String code) {
    return getTags().stream()
        .filter(tag -> code.equals(tag.getCode()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing required `" + code + "` tag."));
  }

  /**
   * Ensure that at least one tag instance of the provided class exists.
   *
   * @param clazz the tag class to search for
   * @param <T> tag type
   * @return the first matching tag instance
   * @throws AssertionError if no matching tag is present
   */
  protected <T extends BaseTag> T requireTagInstance(@NonNull Class<T> clazz) {
    return getTags().stream()
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .findFirst()
        .orElseThrow(
            () -> new AssertionError("Missing required `" + clazz.getSimpleName() + "` tag."));
  }

  public static <T extends GenericEvent> T convert(
      @NonNull GenericEvent genericEvent, @NonNull Class<T> clazz) throws NostrException {
    try {
      T event = clazz.getConstructor().newInstance();
      event.setContent(genericEvent.getContent());
      event.setTags(genericEvent.getTags());
      event.setPubKey(genericEvent.getPubKey());
      event.setId(genericEvent.getId());
      event.set_serializedEvent(genericEvent.get_serializedEvent());
      event.setNip(genericEvent.getNip());
      event.setKind(genericEvent.getKind());
      event.setSignature(genericEvent.getSignature());
      event.setCreatedAt(genericEvent.getCreatedAt());
      return event;
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new NostrException("Failed to convert GenericEvent", e);
    }
  }
}
