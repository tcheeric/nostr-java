package nostr.event.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;

/**
 *
 * @author eric
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
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
        var index = 0;

        var attrList = getSupportedAttributes(relay);
        for (var a : attrList) {
            final List valueList = a.getValueList();

            int i = 0;            
            result.append(",");
            
            for (var value : valueList) {
                if (!escape) {
                    result.append("\"");
                } else {
                    result.append("\\\"");
                }

                result.append(value);

                if (!escape) {
                    result.append("\"");
                } else {
                    result.append("\\\"");
                }

                if (++i < valueList.size()) {
                    result.append(",");
                }
            }

            if (++index < attrList.size()) {
                result.append(",");
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
