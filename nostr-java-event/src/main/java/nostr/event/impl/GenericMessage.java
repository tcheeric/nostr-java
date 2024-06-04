package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.ElementAttribute;
import nostr.base.IElement;
import nostr.base.IEncoder;
import nostr.base.IGenericElement;
import nostr.event.BaseMessage;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */

@Setter
@Getter
public class GenericMessage extends BaseMessage implements IGenericElement, IElement {

    @JsonIgnore
    private final List<ElementAttribute> attributes;

    @JsonIgnore
    private final Integer nip;

    public GenericMessage(String command) {
        this(command, new ArrayList<>(), 1);
    }

    public GenericMessage(String command, Integer nip) {
        this(command, new ArrayList<>(), nip);
    }

    public GenericMessage(String command, List<ElementAttribute> attributes, Integer nip) {
        super(command);
        this.attributes = attributes;
        this.nip = nip;
    }

    @Override
    public void addAttribute(ElementAttribute attribute) {
        this.attributes.add(attribute);
    }

    @Override
    public String encode() throws JsonProcessingException {
        getArrayNode().add(getCommand());
        getAttributes().stream().map(ElementAttribute::getValue).forEach(v -> getArrayNode().add(v.toString()));
        return IEncoder.MAPPER.writeValueAsString(getArrayNode());
    }

    public static <T extends BaseMessage> T decode(@NonNull Object[] msgArr) {
        GenericMessage gm = new GenericMessage(msgArr[0].toString());
        for (int i = 1; i < msgArr.length; i++) {
            if (msgArr[i] instanceof String) {
                gm.addAttribute(ElementAttribute.builder().value(msgArr[i]).build());
            }
        }
        return (T) gm;
    }
}
