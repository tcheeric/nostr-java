package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nostr.base.GenericTagQuery;
import nostr.base.IEncoder;
import nostr.event.list.GenericTagQueryList;

import java.io.IOException;

/**
 * @author guilhermegps
 *
 */
public class CustomGenericTagQueryListSerializer<T extends GenericTagQueryList<U>, U extends GenericTagQuery> extends JsonSerializer<T> {

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (U gtq : value.getList()) {
            JsonNode node = toJson(gtq);
            gen.writeObjectField(node.fieldNames().next(), node.get(node.fieldNames().next()));
        }
        gen.writeEndObject();
    }

    private JsonNode toJson(GenericTagQuery gtq) {
        var mapper = IEncoder.MAPPER;
        try {
            JsonNode node = mapper.valueToTree(gtq);
            ObjectNode objNode = (ObjectNode) node;
            objNode.set("#" + node.get("tagName").textValue(), node.get("value"));
            objNode.remove("tagName");
            objNode.remove("value");
            objNode.remove("nip");

            return node.get("genericTagQueryList").get(0);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

}
