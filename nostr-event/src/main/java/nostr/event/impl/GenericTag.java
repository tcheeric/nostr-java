package nostr.event.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.types.MarshallException;
import nostr.types.values.marshaller.BaseTypesMarshaller;

/**
 *
 * @author squirrel
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@Log
public class GenericTag extends BaseTag implements IGenericElement {

    private final Integer nip;
    private final String code;
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
        for (var a : supportedAttributes) {
            try {
                return BaseTypesMarshaller.Factory.create(a.getValue()).marshall();
                
            } catch (MarshallException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }

        return result.toString();
    }

    private Set<ElementAttribute> getSupportedAttributes(Relay relay) {
        Set<ElementAttribute> result = new HashSet<>();
        for (var a : this.attributes) {
            if (relay.getSupportedNips().contains(a.getNip())) {
                result.add(a);
            }
        }
        return result;
    }
}
