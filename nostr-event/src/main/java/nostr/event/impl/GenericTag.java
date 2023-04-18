package nostr.event.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.marshaller.impl.TagMarshaller;
import nostr.types.MarshallException;
import nostr.types.values.marshaller.BaseTypesMarshaller;
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
    private final Integer nip;
	@JsonIgnore
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
    public String printAttributes(Relay relay, boolean escape) {
        var result = new StringBuilder();

        var supportedAttributes = getSupportedAttributes(relay);

        if (supportedAttributes.size() >= 1) {
            result.append(",");
        }

        var i = 0;
        for (var a : supportedAttributes) {
            try {
                result.append(BaseTypesMarshaller.Factory.create(a.getValue()).marshall());

                if (++i < supportedAttributes.size()) {
                    result.append(",");
                }
                
            } catch (MarshallException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }

        return result.toString();
    }

    @Override
    public String toString() {
        try {
            return new TagMarshaller(this, null).marshall();
        } catch (NostrException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private Set<ElementAttribute> getSupportedAttributes(Relay relay) {
        Set<ElementAttribute> result = new HashSet<>();
        for (var a : this.attributes) {
            if (relay == null || relay.getSupportedNips().contains(a.getNip())) {
                result.add(a);
            }
        }
        return result;
    }
}
