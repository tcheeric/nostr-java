package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.base.ISignable;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.annotation.Key;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseEvent;
import nostr.event.BaseTag;
import nostr.event.Deleteable;
import nostr.event.Kind;
import nostr.event.json.deserializer.PublicKeyDeserializer;
import nostr.event.json.deserializer.SignatureDeserializer;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import nostr.util.thread.HexStringValidator;

import java.beans.Transient;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;

/**
 * @author squirrel
 */
@Log
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
        this.tags = tags;
        this.content = content;
        this.attributes = new ArrayList<>();

        // Update parents
        updateTagsParents(tags);
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

        this.tags = tags;

        for (ITag o : tags) {
            o.setParent(this);
        }
    }

    public void addTag(BaseTag tag) {
        if (tags == null)
            tags = new ArrayList<>();

        if (!tags.contains(tag)) {
            tag.setParent(this);
            tags.add(tag);
        }
    }

    public void update() {

        try {
            this.validate();

            this.createdAt = Instant.now().getEpochSecond();

            this._serializedEvent = this.serialize().getBytes(StandardCharsets.UTF_8);

            this.id = NostrUtil.bytesToHex(NostrUtil.sha256(_serializedEvent));
        } catch (NostrException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (AssertionError ex) {
            log.log(Level.WARNING, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Transient
    public boolean isSigned() {
        return this.signature != null;
    }

    @Override
    public void addAttribute(ElementAttribute... attribute) {
        addAttributes(List.of(attribute));
    }

    @Override
    public void addAttributes(List<ElementAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    protected void validate() {

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
            throw new NostrException(e);
        }
    }

    @Transient
    @Override
    public Consumer<Signature> getSignatureConsumer() {
        return this::setSignature;
    }

    @Transient
    @Override
    public Supplier<ByteBuffer> getByeArraySupplier() {
        this.update();
        log.log(Level.FINER, "Serialized event: {0}", new String(this.get_serializedEvent()));
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
        Optional.ofNullable(value).ifPresent(s -> addTag(GenericTag.create(key, s.toString())));
    }

    protected void addStringListTag(String label, Integer nip, List<String> tag) {
        Optional.ofNullable(tag).ifPresent(tagList -> GenericTag.create(label, tagList));
    }
}
