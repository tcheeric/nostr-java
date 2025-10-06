package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.beans.Transient;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Builder;
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
import nostr.util.validator.HexStringValidator;
import nostr.event.support.GenericEventConverter;
import nostr.event.support.GenericEventTypeClassifier;
import nostr.event.support.GenericEventUpdater;
import nostr.event.support.GenericEventValidator;
import nostr.util.NostrException;

/**
 * @author squirrel
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

  @JsonIgnore @EqualsAndHashCode.Exclude private byte[] serializedEventCache;

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
    this.kind = Kind.valueOf(kind).getValue();
    this.tags = new ArrayList<>(tags);
    this.content = content;

    // Update parents
    updateTagsParents(this.tags);
  }

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
      Optional.ofNullable(id).ifPresent(event::setId);
      event.setPubKey(pubKey);

      if (customKind == null && kind == null) {
        throw new IllegalArgumentException("A kind value must be provided when building a GenericEvent.");
      }

      if (customKind != null) {
        event.setKind(customKind);
      } else {
        event.setKind(kind.getValue());
      }

      event.setTags(Optional.ofNullable(tags).map(ArrayList::new).orElseGet(ArrayList::new));
      event.setContent(Optional.ofNullable(content).orElse(""));
      event.setCreatedAt(createdAt);
      event.setSignature(signature);
      event.setNip(nip);
      return event;
    }
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

  @Transient
  public boolean isReplaceable() {
    return GenericEventTypeClassifier.isReplaceable(this.kind);
  }

  @Transient
  public boolean isEphemeral() {
    return GenericEventTypeClassifier.isEphemeral(this.kind);
  }

  @Transient
  public boolean isAddressable() {
    return GenericEventTypeClassifier.isAddressable(this.kind);
  }

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

  public void update() {
    GenericEventUpdater.refresh(this);
  }

  @Transient
  public boolean isSigned() {
    return this.signature != null;
  }

  public void validate() {
    GenericEventValidator.validate(this);
  }

  protected void validateKind() {
    GenericEventValidator.validateKind(this.kind);
  }

  protected void validateTags() {
    GenericEventValidator.validateTags(this.tags);
  }

  protected void validateContent() {
    GenericEventValidator.validateContent(this.content);
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
      log.trace("Serialized event: {}", new String(this.getSerializedEventCache()));
    }
    return () -> ByteBuffer.wrap(this.getSerializedEventCache());
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
    return GenericEventConverter.convert(genericEvent, clazz);
  }
}
