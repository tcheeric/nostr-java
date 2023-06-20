package nostr.event.impl;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.logging.Level;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.event.BaseTag;
import nostr.event.json.codec.ElementEncoder;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Log
public class GenericTag extends BaseTag implements IGenericElement {
    
    private final String code;
    
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final Integer nip;
    
    //@JsonIgnore
    private final Set<ElementAttribute> attributes;

    public GenericTag(String code) {
        this(1, code, new HashSet<>());
    }

    public GenericTag(Integer nip, String code) {
        this(nip, code, new HashSet<>());
    }

    public GenericTag(Integer nip, String code, Set<ElementAttribute> attributes) {
        this.nip = nip;
        this.code = code;
        this.attributes = attributes;
    }

    @Override
    public void addAttribute(ElementAttribute attribute) {
        this.attributes.add(attribute);
    }

    @Override
    public String toString() {
        try {
            return new ElementEncoder(this).encode();
        } catch (NostrException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
