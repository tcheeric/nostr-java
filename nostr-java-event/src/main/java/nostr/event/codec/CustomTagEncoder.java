package nostr.event.codec;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.lang.reflect.Field;
import java.util.List;

import lombok.extern.java.Log;
import nostr.event.BaseTag;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@Log
public class CustomTagEncoder extends StdSerializer<BaseTag> {

    private static final long serialVersionUID = -3877972991082754068L;

    public CustomTagEncoder() {
        super(BaseTag.class);
    }

    @Override
    public void serialize(BaseTag value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            // -- Create the node
            final ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
            List<Field> fields = value.getSupportedFields(null);

            fields.stream().forEach((Field f) -> {
                try {
                    node.put(f.getName(), value.getFieldValue(f));
                } catch (NostrException ex) {
                    throw new RuntimeException(ex);
                }
            });

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
