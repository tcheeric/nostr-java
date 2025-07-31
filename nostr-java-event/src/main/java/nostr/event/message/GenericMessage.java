package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.ElementAttribute;
import nostr.base.IElement;
import nostr.base.IGenericElement;
import nostr.event.BaseMessage;

import java.util.ArrayList;
import java.util.List;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;

/**
 * @author squirrel
 */

@Setter
@Getter
public class GenericMessage extends BaseMessage implements IGenericElement, IElement {

    @JsonIgnore
    private final List<ElementAttribute> attributes;

    public GenericMessage(String command) {
        this(command, new ArrayList<>());
    }

    public GenericMessage(String command, List<ElementAttribute> attributes) {
        super(command);
        this.attributes = attributes;
    }

    @Override
    public void addAttribute(ElementAttribute... attribute) {
        addAttributes(List.of(attribute));
    }

    @Override
    public void addAttributes(List<ElementAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    @Override
    public String encode() throws JsonProcessingException {
        var encoderArrayNode = JsonNodeFactory.instance.arrayNode();
        encoderArrayNode.add(getCommand());
        getAttributes().stream().map(ElementAttribute::getValue).forEach(v -> encoderArrayNode.add(v.toString()));
        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(encoderArrayNode);
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
