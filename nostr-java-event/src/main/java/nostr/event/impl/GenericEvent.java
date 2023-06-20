package nostr.event.impl;

import java.beans.Transient;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.ArrayList;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.IEncoder;
import nostr.base.IGenericElement;
import nostr.base.ISignable;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.annotation.JsonString;
import nostr.base.annotation.Key;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseEvent;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.json.deserializer.PublicKeyDeserializer;
import nostr.event.json.deserializer.SignatureDeserializer;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
public class GenericEvent extends BaseEvent implements ISignable, IGenericElement {

    @Key
    @EqualsAndHashCode.Include
    private String id;

    @Key
    @JsonProperty("pubkey")
    @EqualsAndHashCode.Include
    @JsonString
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
    private List<? extends BaseTag> tags;

    @Key
    @EqualsAndHashCode.Exclude
    private String content;

    @Key
    @JsonProperty("sig")
    @EqualsAndHashCode.Exclude
    @JsonString
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
    private final Set<ElementAttribute> attributes;

    public GenericEvent() {
        this.attributes = new HashSet<>();
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind) {
        this(pubKey, kind, new ArrayList<>(), null);
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<? extends BaseTag> tags) {
        this(pubKey, kind, tags, null);
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull List<? extends BaseTag> tags, String content) {
        this(pubKey, kind.getValue(), tags, content);
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Integer kind, @NonNull List<? extends BaseTag> tags, String content) {
        this.pubKey = pubKey;
        this.kind = kind;
        this.tags = tags;
        this.content = content;
        this.attributes = new HashSet<>();

        // Update parents
        updateTagsParents(tags);
    }

    @Override
    public String toBech32() {
        if (!isSigned()) {
            this.update();
        }

        try {
            return Bech32.toBech32(Bech32Prefix.NOTE, this.getId());
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setTags(List<? extends BaseTag> tags) {

        this.tags = tags;

        for (Object o : tags) {
            ((ITag) o).setParent(this);
        }
    }

    public void addTag(BaseTag tag) {

        if (!tags.contains(tag)) {
            tag.setParent(this);
            ((List<BaseTag>) tags).add(tag);
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
        }
    }

    @Transient
    public boolean isSigned() {
        return this.signature != null;
    }

    @Override
    public void addAttribute(ElementAttribute attribute) {
        this.attributes.add(attribute);
    }

    protected void validate() {

    }

    protected static String escapeJsonString(String jsonString) {
        return jsonString.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @SuppressWarnings("unchecked")
    private String serialize() throws NostrException {
        var mapper = IEncoder.MAPPER;
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

    protected final void updateTagsParents(List<? extends BaseTag> tagList) {
        if (tagList != null && !tagList.isEmpty()) {
            for (Object t : tagList) {
                ITag tag = (ITag) t;
                tag.setParent(this);
            }
        }
    }

}
