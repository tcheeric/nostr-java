package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.ElementAttribute;
import nostr.base.IElement;
import nostr.base.IGenericElement;
import nostr.event.BaseMessage;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericMessage extends BaseMessage implements IGenericElement, IElement {

//    @JsonProperty
//    private final String command;
    
    @JsonIgnore
    private final Set<ElementAttribute> attributes;
    
    @JsonIgnore
    private final Integer nip;
    
    public GenericMessage(String command) {
        this(command, new HashSet<>(), 1);
    }

    public GenericMessage(String command, Integer nip) {
        this(command, new HashSet<>(), nip);
    }

    public GenericMessage(String command, Set<ElementAttribute> attributes, Integer nip) {
        super(command);
        this.attributes = attributes;
        this.nip = nip;
    }

    @Override
    public void addAttribute(ElementAttribute attribute) {
        this.attributes.add(attribute);
    }

}
