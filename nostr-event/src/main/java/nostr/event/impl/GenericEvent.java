package nostr.event.impl;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import lombok.Data;
import java.beans.Transient;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import lombok.NonNull;
import nostr.base.Bech32Prefix;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.base.ISignable;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.annotation.JsonString;
import nostr.base.annotation.Key;
import nostr.crypto.bech32.Bech32;
import nostr.event.BaseEvent;
import nostr.event.Kind;
import nostr.event.list.TagList;
import nostr.event.marshaller.impl.EventMarshaller;
import nostr.event.marshaller.impl.TagListMarshaller;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import nostr.util.UnsupportedNIPException;

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

    @Key(name = "pubkey")
    @EqualsAndHashCode.Include
    @JsonString
    private PublicKey pubKey;

    @Key(name = "created_at")
    @EqualsAndHashCode.Exclude
    private Long createdAt;

    @Key
    @EqualsAndHashCode.Exclude
    private Integer kind;

    @Key
    @EqualsAndHashCode.Exclude
    private TagList tags;

    @Key
    @EqualsAndHashCode.Exclude
    private String content;

    @Key(name = "sig")
    @EqualsAndHashCode.Exclude
    @JsonString
    private Signature signature;

    @EqualsAndHashCode.Exclude
    private byte[] _serializedEvent;
    
    @EqualsAndHashCode.Exclude
    private Integer nip;

    @EqualsAndHashCode.Exclude
    private final Set<ElementAttribute> attributes;

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind) {
        this(pubKey, kind, new TagList(), null);
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull TagList tags) {
        this(pubKey, kind, tags, null);
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull TagList tags, String content) {
        this(pubKey, kind.getValue(), tags, content);
    }

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Integer kind, @NonNull TagList tags, String content) {
        this.pubKey = pubKey;
        this.kind = kind;
        this.tags = tags;
        this.content = content;
        this.attributes = new HashSet<>();

        // Update parents
        updateTagsParents(tags);
    }

    @Override
    public String toString() {
        try {
            return new EventMarshaller(this, null).marshall();
        } catch (UnsupportedNIPException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toBech32() {
        if (!isSigned()) {
            try {
                this.update();
            } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException | NostrException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
        try {
            return Bech32.toBech32(Bech32Prefix.NOTE.getCode(), this.getId());
        } catch (NostrException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public void setTags(TagList tags) {

        @SuppressWarnings("rawtypes")
        List list = tags.getList();

        for (Object o : list) {
            ((ITag) o).setParent(this);
        }
    }

    public void addTag(ITag tag) {
        List list = tags.getList();

        if (!list.contains(tag)) {
            tag.setParent(this);
            list.add(tag);
        }
    }

    public void update() throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        this.createdAt = Instant.now().getEpochSecond();

        this._serializedEvent = this.serialize().getBytes(StandardCharsets.UTF_8);

        this.id = NostrUtil.bytesToHex(NostrUtil.sha256(_serializedEvent));
    }

    @Transient
    public boolean isSigned() {
        return this.signature != null;
    }

    @Override
    public void addAttribute(ElementAttribute attribute) {
        this.attributes.add(attribute);
    }

    @SuppressWarnings("unchecked")
    private String serialize() throws NostrException {
        var sb = new StringBuilder();
        sb.append("[");
        sb.append("0").append(",\"");
        sb.append(this.pubKey).append("\",");
        sb.append(this.createdAt).append(",");
        sb.append(this.kind).append(",");

        sb.append(new TagListMarshaller(tags, null).marshall());
        sb.append(",\"");
        sb.append(this.content);
        sb.append("\"]");

        return sb.toString();
    }

    private void updateTagsParents(TagList tagList) {

        if (tagList != null && !tagList.getList().isEmpty()) {
            for (Object t : tagList.getList()) {
                ITag tag = (ITag) t;
                tag.setParent(this);
            }
        }
    }

}
