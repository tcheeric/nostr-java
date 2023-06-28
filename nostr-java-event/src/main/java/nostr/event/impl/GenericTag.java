package nostr.event.impl;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.event.BaseTag;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Log
@AllArgsConstructor
public class GenericTag extends BaseTag implements IGenericElement {

    private final String code;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final Integer nip;

    private final Set<ElementAttribute> attributes;

    public GenericTag(String code) {
        this(code, 1);
    }

    public GenericTag(String code, Integer nip) {
        this(code, nip, new HashSet<>());
    }

    @Override
    public void addAttribute(ElementAttribute attribute) {
        this.attributes.add(attribute);
    }

}
