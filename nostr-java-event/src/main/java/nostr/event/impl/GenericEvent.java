package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
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
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import nostr.util.validator.HexStringValidator;

import java.beans.Transient;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;

/**
 * @author squirrel
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericEvent extends BaseEvent implements ISignable, IGenericElement, Deleteable {

    @Key
    @EqualsAndHashCode.Include
    private String id;

    @Key
    @JsonProperty("pubkey")
    @EqualsAndHashCode.Include
    @JsonDeserialize(using = PublicKeyDeserializer.class)
    private PublicKey pubKey;

    @Key
    @JsonProperty("created_at")
    @EqualsAndHashCode.Exclude
    private Long createdAt;

    @Key
    @EqualsAndHashCode.Exclude
    private Integer kind;

    @Key
    @EqualsAndHashCode.Exclude
    @JsonProperty("tags")
    private List<BaseTag> tags;

    @Key
    @EqualsAndHashCode.Exclude
    private String content;

    @Key
    @JsonProperty("sig")
    @EqualsAndHashCode.Exclude
    @JsonDeserialize(using = SignatureDeserializer.class)
    private Signature signature;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private byte[] _serializedEvent;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Integer nip;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Deprecated
    private final List<ElementAttribute> attributes;

    public GenericEvent() {
        this.attributes = new ArrayList<>();
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

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<BaseTag> tags,
                        @NonNull String content) {
        this(pubKey, kind.getValue(), tags, content);
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Integer kind, @NonNull List<BaseTag> tags,
                        @NonNull String content) {
        this.pubKey = pubKey;
        this.kind = Kind.valueOf(kind).getValue();
        this.tags = new ArrayList<>(tags);
        this.content = content;
        this.attributes = new ArrayList<>();

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

    @Transient
    public boolean isReplaceable() {
        return this.kind != null && this.kind >= 10000 && this.kind < 20000;
    }

    @Transient
    public boolean isEphemeral() {
        return this.kind != null && this.kind >= 20000 && this.kind < 30000;
    }

    @Transient
    public boolean isAddressable() {
        return this.kind != null && this.kind >= 30000 && this.kind < 40000;
    }

    public void addTag(BaseTag tag) {
        if (tag == null) {
            return;
        }

        if (tags == null)
            tags = new ArrayList<>();

        if (!tags.contains(tag)) {
            tag.setParent(this);
            tags.add(tag);
        }
    }

    public void update() {

        try {
            this.createdAt = Instant.now().getEpochSecond();

            this._serializedEvent = this.serialize().getBytes(StandardCharsets.UTF_8);

            this.id = NostrUtil.bytesToHex(NostrUtil.sha256(_serializedEvent));
        } catch (NostrException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (AssertionError ex) {
            log.warn(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Transient
    public boolean isSigned() {
        return this.signature != null;
    }

    @Deprecated
    @Override
    public void addAttribute(ElementAttribute... attribute) {
        addAttributes(List.of(attribute));
    }

    @Deprecated
    @Override
    public void addAttributes(List<ElementAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    public void validate() {

        // Validate `id` field
        HexStringValidator.validateHex(this.id, 64);

        // Validate `pubkey` field
        HexStringValidator.validateHex(this.pubKey.toString(), 64);

        // Validate `sig` field
        HexStringValidator.validateHex(this.signature.toString(), 128);

        // Validate `created_at` field
        if (this.createdAt == null || this.createdAt < 0) {
            throw new AssertionError("Invalid `created_at`: Must be a non-negative integer.");
        }

        validateKind();

        validateTags();

        validateContent();
    }

    protected void validateKind() {
        if (this.kind == null || this.kind < 0) {
            throw new AssertionError("Invalid `kind`: Must be a non-negative integer.");
        }
    }

    protected void validateTags() {
        if (this.tags == null) {
            throw new AssertionError("Invalid `tags`: Must be a non-null array.");
        }
    }

    protected void validateContent() {
        if (this.content == null) {
            throw new AssertionError("Invalid `content`: Must be a string.");
        }
    }

    private String serialize() throws NostrException {
        var mapper = ENCODER_MAPPED_AFTERBURNER;
        var arrayNode = JsonNodeFactory.instance.arrayNode();

        try {
            arrayNode.add(0);
            arrayNode.add(this.pubKey.toString());
            arrayNode.add(this.createdAt);
            arrayNode.add(this.kind);
            arrayNode.add(mapper.valueToTree(tags));
            arrayNode.add(this.content);

            return mapper.writeValueAsString(arrayNode);
        } catch (JsonProcessingException e) {
            throw new NostrException(e.getMessage());
        }
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
        log.debug("Serialized event: {}", new String(this.get_serializedEvent()));
        return () -> ByteBuffer.wrap(this.get_serializedEvent());
    }

    @Deprecated
    public ElementAttribute getAttribute(@NonNull String name) {
        return this.attributes.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
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
        return getTags().stream()
                .filter(tag -> code.equals(tag.getCode()))
                .findFirst()
                .orElseThrow();
    }

    protected List<BaseTag> getTags(@NonNull String code) {
        return getTags().stream()
                .filter(tag -> code.equals(tag.getCode()))
                .toList();
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
     * @param <T>   tag type
     * @return the first matching tag instance
     * @throws AssertionError if no matching tag is present
     */
    protected <T extends BaseTag> T requireTagInstance(@NonNull Class<T> clazz) {
        return getTags().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing required `" + clazz.getSimpleName() + "` tag."));
    }


    public static <T extends GenericEvent> T convert(@NonNull GenericEvent genericEvent, @NonNull Class<T> clazz) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
