package nostr.event.impl;

import java.beans.IntrospectionException;
import java.beans.Transient;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.base.IMarshaller;
import nostr.base.ISignable;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.annotation.JsonString;
import nostr.base.annotation.Key;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseEvent;
import nostr.event.Kind;
import nostr.event.list.TagList;
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
    private TagList tags;

    @Key
    @EqualsAndHashCode.Exclude
    private String content;

    @Key
    @JsonProperty("sig")
    @EqualsAndHashCode.Exclude
    @JsonString
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
            return Bech32.toBech32(Bech32Prefix.NOTE, this.getId());
        } catch (NostrException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public void setTags(TagList tags) {

        this.tags = tags;

        for (Object o : tags.getList()) {
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
    	var mapper = IMarshaller.MAPPER;
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

    protected final void updateTagsParents(TagList tagList) {
        if (tagList != null && !tagList.getList().isEmpty()) {
            for (Object t : tagList.getList()) {
                ITag tag = (ITag) t;
                tag.setParent(this);
            }
        }
    }

}
