package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nostr.event.BaseTag;

import java.io.IOException;

import static nostr.event.json.codec.BaseTagEncoder.BASETAG_ENCODER_MAPPER_BLACKBIRD;

abstract class AbstractTagSerializer<T extends BaseTag> extends StdSerializer<T> {
    protected AbstractTagSerializer(Class<T> t) {
        super(t);
    }

    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            final ObjectNode node = BASETAG_ENCODER_MAPPER_BLACKBIRD.getNodeFactory().objectNode();
            value.getSupportedFields().forEach(f ->
                value.getFieldValue(f)
                    .ifPresent(s ->
                        node.put(f.getName(), s)));

            applyCustomAttributes(node, value);

            ArrayNode arrayNode = node.objectNode().putArray("values").add(value.getCode());
            node.fields().forEachRemaining(entry -> arrayNode.add(entry.getValue().asText()));
            gen.writePOJO(arrayNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void applyCustomAttributes(ObjectNode node, T value) {
    }
}
