package nostr.event.impl;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.event.BaseTag;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class GenericTag extends BaseTag implements IGenericElement {

    private final String code;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final Integer nip;

    private final List<ElementAttribute> attributes;

    public GenericTag(String code) {
        this(code, 1);
    }

    public GenericTag(String code, Integer nip) {
        this(code, nip, new ArrayList<>());
    }

    @Override
    public void addAttribute(ElementAttribute attribute) {
        this.attributes.add(attribute);
    }

    public static GenericTag create(String code, Integer nip, List<String> params) {
        List<ElementAttribute> attributes = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            String name = "param" + i;
            var p = params.get(i);
            attributes.add(i, ElementAttribute.builder().name(name).value(p).build());
        }
        return new GenericTag(code, nip, attributes);
    }

    public static GenericTag create(String code, Integer nip, String param) {
        List<ElementAttribute> attributes = new ArrayList<>();
        attributes.add(0, ElementAttribute.builder().name("param0").value(param).build());
        return new GenericTag(code, nip, attributes);
    }
}
