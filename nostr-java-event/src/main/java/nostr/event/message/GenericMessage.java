package nostr.event.message;

import static nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.ElementAttribute;
import nostr.base.IElement;
import nostr.base.IGenericElement;
import nostr.event.BaseMessage;
import nostr.event.json.codec.EventEncodingException;

/**
 * @author squirrel
 */
@Setter
@Getter
public class GenericMessage extends BaseMessage implements IGenericElement, IElement {

  @JsonIgnore private final List<ElementAttribute> attributes;

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
  public String encode() throws EventEncodingException {
    var encoderArrayNode = JsonNodeFactory.instance.arrayNode();
    encoderArrayNode.add(getCommand());
    getAttributes().stream()
        .map(ElementAttribute::value)
        .forEach(v -> encoderArrayNode.add(v.toString()));
    try {
      return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(encoderArrayNode);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode generic message", e);
    }
  }

  // Generics are erased at runtime; BaseMessage subtype is determined by caller context
  @SuppressWarnings("unchecked")
  public static <T extends BaseMessage> T decode(@NonNull Object[] msgArr) {
    GenericMessage gm = new GenericMessage(msgArr[0].toString());
    for (int i = 1; i < msgArr.length; i++) {
      if (msgArr[i] instanceof String) {
        gm.addAttribute(new ElementAttribute(null, msgArr[i]));
      }
    }
    return (T) gm;
  }
}
