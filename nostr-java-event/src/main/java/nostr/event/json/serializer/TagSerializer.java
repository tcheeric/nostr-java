package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.impl.GenericTag;
import nostr.util.NostrException;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Field;
import java.util.List;

import static nostr.event.json.codec.BaseTagEncoder.BASETAGENCODER_MAPPED_AFTERBURNER;

/**
 * @author guilhermegps
 *
 */
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
            List<Field> fields = value.getSupportedFields();

            // Populate the node with the fields data
            fields.forEach((Field f) -> {
                try {
                    node.put(f.getName(), value.getFieldValue(f));
                } catch (NostrException ex) {
                    throw new RuntimeException(ex);
                }
            });

            // Populate the node with the attributes data
            if (value instanceof GenericTag genericTag) {
                List<ElementAttribute> attrs = genericTag.getAttributes();
                attrs.forEach(a -> node.put(a.getName(), a.getValue().toString()));
            }

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
