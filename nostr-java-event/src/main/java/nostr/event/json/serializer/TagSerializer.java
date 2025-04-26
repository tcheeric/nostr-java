package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;
import nostr.util.NostrException;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import static nostr.event.json.codec.BaseTagEncoder.BASETAGENCODER_MAPPED_AFTERBURNER;

/**
 * @author guilhermegps
 */
@Log
public class TagSerializer extends StdSerializer<BaseTag> {

    @Serial
    private static final long serialVersionUID = -3877972991082754068L;

    public TagSerializer() {
        super(BaseTag.class);
    }

    @Override
    public void serialize(BaseTag value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            // -- Create the node
            final ObjectNode node = BASETAGENCODER_MAPPED_AFTERBURNER.getNodeFactory().objectNode();

            log.log(Level.INFO, ">>>>>>>>>> Serializing tag: {0}", value);

            if (value instanceof GenericTag && value.getClass() != GenericTag.class) {
                // value is a subclass of GenericTag
                List<Field> fields = value.getSupportedFields();

                // Populate the node with the fields data
                fields.forEach((Field f) -> {
                    try {
                        node.put(f.getName(), value.getFieldValue(f));
                    } catch (NostrException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } else {
                // value is not a subclass of GenericTag
                // Populate the node with the attributes data
                GenericTag genericTag = (GenericTag) value;
                List<ElementAttribute> attrs = genericTag.getAttributes();
                attrs.forEach(a -> node.put(a.getName(), a.getValue().toString()));
            }

            log.log(Level.INFO, ">>>>>>>>> Serialized node: {0}", node);

            // Extract the property values from the node and serialize them as an array
            if (node.isObject()) {
                ArrayNode arrayNode = node.objectNode().putArray("values");

                // Add the tag code as the first element
                arrayNode.add(value.getCode());
                node.fields().forEachRemaining(entry -> arrayNode.add(entry.getValue().asText()));

                gen.writePOJO(arrayNode);
            } else {
                throw new AssertionError("node.isObject()", new RuntimeException());
            }

        } catch (IOException | NostrException e) {
            throw new RuntimeException(e);
        }
    }

}
