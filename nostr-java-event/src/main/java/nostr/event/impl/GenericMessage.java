package nostr.event.impl;

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
import java.util.stream.IntStream;
import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;
import static nostr.base.IDecoder.I_DECODER_MAPPER_AFTERBURNER;

@Setter
@Getter
public class GenericMessage extends BaseMessage implements IGenericElement, IElement {
    @JsonIgnore
    private final List<ElementAttribute> attributes;

    public GenericMessage(@NonNull String command) {
        this(command, new ArrayList<>());
    }

    /**
     * nip ctor parameter to be removed
     *
     * @deprecated use any available proper constructor variant instead
     */
    @Deprecated(forRemoval = true)
    public GenericMessage(@NonNull String command, @NonNull Integer nip) {
        this(command, new ArrayList<>());
    }

    public GenericMessage(@NonNull String command, @NonNull List<ElementAttribute> attributes) {
        super(command);
        this.attributes = attributes;
    }

    /**
     * nip ctor parameter to be removed
     *
     * @deprecated use any available proper constructor variant instead
     */
    @Deprecated(forRemoval = true)
    public GenericMessage(@NonNull String command, @NonNull List<ElementAttribute> attributes, @NonNull Integer nip) {
        this(command, attributes);
    }

    @Override
    public void addAttribute(@NonNull ElementAttribute... attribute) {
        addAttributes(List.of(attribute));
    }

    @Override
    public void addAttributes(@NonNull List<ElementAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    @Override
    public String encode() throws JsonProcessingException {
        var encoderArrayNode = JsonNodeFactory.instance.arrayNode();
        encoderArrayNode.add(getCommand());
        getAttributes().stream().map(ElementAttribute::getValue).forEach(v -> encoderArrayNode.add(v.toString()));
        return ENCODER_MAPPED_AFTERBURNER.writeValueAsString(encoderArrayNode);
    }

//    public static <T extends BaseMessage> T decode(@NonNull String jsonString) {
//        try {
//            Object[] msgArr = I_DECODER_MAPPER_AFTERBURNER.readValue(jsonString, Object[].class);
//            GenericMessage gm = new GenericMessage(msgArr[0].toString());
//            for (int i = 1; i < msgArr.length; i++) {
////                TODO: does below ever resolve to String?  because RxR stream says it'll always be false.  check eric's tests and see what's happening there
//                if (msgArr[i] instanceof String) {
//                    gm.addAttribute(ElementAttribute.builder().value(msgArr[i]).build());
//                }
//            }
//            return (T) gm;
//        } catch (Exception e) {
//            throw new AssertionError(e);
//        }
//    }

    public static <T extends BaseMessage> T decode(@NonNull String json) {
        try {
            Object[] msgArr = I_DECODER_MAPPER_AFTERBURNER.readValue(json, Object[].class);
            GenericMessage gm = new GenericMessage(
                msgArr[0].toString(),
                IntStream.of(1, msgArr.length-1)
                    .mapToObj(i -> ElementAttribute.builder().value(msgArr[i]).build())
                    .distinct()
                    .toList());
            return (T) gm;
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
