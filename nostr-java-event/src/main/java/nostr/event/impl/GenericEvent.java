package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ISignable;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.json.deserializer.PublicKeyDeserializer;
import nostr.event.json.deserializer.SignatureDeserializer;
import nostr.event.serializer.EventSerializer;
import nostr.event.validator.EventValidator;
import nostr.util.NostrException;
import nostr.util.validator.HexStringValidator;

import java.beans.Transient;
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
 * @author squirrel
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
 */
@Slf4j
@Data
@EqualsAndHashCode
public class GenericEvent implements ISignable {

  @EqualsAndHashCode.Include private String id;

@JsonProperty("pubkey")
  @EqualsAndHashCode.Include
  @JsonDeserialize(using = PublicKeyDeserializer.class)
  private PublicKey pubKey;

@JsonProperty("created_at")
  @EqualsAndHashCode.Exclude
  private Long createdAt;

  @EqualsAndHashCode.Exclude private Integer kind;

@EqualsAndHashCode.Exclude
  @JsonProperty("tags")
  private List<BaseTag> tags;

  @EqualsAndHashCode.Exclude private String content;

@JsonProperty("sig")
  @EqualsAndHashCode.Exclude
  @JsonDeserialize(using = SignatureDeserializer.class)
  private Signature signature;

  @JsonIgnore @EqualsAndHashCode.Exclude private byte[] _serializedEvent;

  @JsonIgnore @EqualsAndHashCode.Exclude private String nip;

  public GenericEvent() {
    this.tags = new ArrayList<>();
  }

  public GenericEvent(@NonNull String id) {
    this();
    setId(id);
  }

  public GenericEvent(@NonNull PublicKey pubKey, int kind) {
    this(pubKey, kind, new ArrayList<>(), "");
  }

  public GenericEvent(
      @NonNull PublicKey pubKey,
      int kind,
      @NonNull List<BaseTag> tags,
      @NonNull String content) {
    this.pubKey = pubKey;
    this.kind = kind;
    this.tags = new ArrayList<>(tags);
    this.content = content;
  }

  public void setId(String id) {
    HexStringValidator.validateHex(id, 64);
    this.id = id;
  }

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
  }

  public List<BaseTag> getTags() {
    return Collections.unmodifiableList(this.tags);
  }

  @Transient
  public boolean isReplaceable() {
    return Kinds.isReplaceable(this.kind);
  }

  @Transient
  public boolean isEphemeral() {
    return Kinds.isEphemeral(this.kind);
  }

  @Transient
  public boolean isAddressable() {
    return Kinds.isAddressable(this.kind);
  }

  public void addTag(BaseTag tag) {
    if (tag == null) {
      return;
    }

    if (tags == null) tags = new ArrayList<>();

    if (!tags.contains(tag)) {
      tags.add(tag);
    }
  }

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

  public static GenericEventBuilder builder() {
    return new GenericEventBuilder();
  }

  public static class GenericEventBuilder {
    private String id;
    private PublicKey pubKey;
    private Integer kind;
    private List<BaseTag> tags = new ArrayList<>();
    private String content = "";
    private Long createdAt;
    private Signature signature;
    private String nip;

    public GenericEventBuilder id(String id) { this.id = id; return this; }
    public GenericEventBuilder pubKey(PublicKey pubKey) { this.pubKey = pubKey; return this; }
    public GenericEventBuilder kind(int kind) { this.kind = kind; return this; }
    public GenericEventBuilder tags(List<BaseTag> tags) { this.tags = tags; return this; }
    public GenericEventBuilder content(String content) { this.content = content; return this; }
    public GenericEventBuilder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
    public GenericEventBuilder signature(Signature signature) { this.signature = signature; return this; }
    public GenericEventBuilder nip(String nip) { this.nip = nip; return this; }

    public GenericEvent build() {
      GenericEvent event = new GenericEvent();
      if (id != null) event.setId(id);
      event.setPubKey(pubKey);

      if (kind == null) {
        throw new IllegalArgumentException("A kind value must be provided when building a GenericEvent.");
      }
      event.setKind(kind);

      event.setTags(tags != null ? new ArrayList<>(tags) : new ArrayList<>());
      event.setContent(content != null ? content : "");
      event.setCreatedAt(createdAt);
      event.setSignature(signature);
      event.setNip(nip);
      return event;
    }
  }

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

  public void validate() {
    EventValidator.validateId(this.id);
    EventValidator.validatePubKey(this.pubKey);
    EventValidator.validateSignature(this.signature);
    EventValidator.validateCreatedAt(this.createdAt);
    validateKind();
    validateTags();
    validateContent();
  }

  protected void validateKind() {
    EventValidator.validateKind(this.kind);
  }

  protected void validateTags() {
    EventValidator.validateTags(this.tags);
  }

  protected void validateContent() {
    EventValidator.validateContent(this.content);
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

  protected BaseTag getTag(@NonNull String code) {
    return getTags().stream().filter(tag -> code.equals(tag.getCode())).findFirst().orElseThrow();
  }

  protected List<BaseTag> getTags(@NonNull String code) {
    return getTags().stream().filter(tag -> code.equals(tag.getCode())).toList();
  }

  protected BaseTag requireTag(@NonNull String code) {
    return getTags().stream()
        .filter(tag -> code.equals(tag.getCode()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing required `" + code + "` tag."));
  }
}
