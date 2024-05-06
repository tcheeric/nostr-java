package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nostr.base.GenericTagQuery;
import nostr.base.IEncoder;

import java.io.IOException;
import java.io.Serial;

/**
 * @author guilhermegps
 */
public class CustomGenericTagQuerySerializer extends StdSerializer<GenericTagQuery> {

    @Serial
    private static final long serialVersionUID = 6803478463890319884L;

    public CustomGenericTagQuerySerializer() {
        super(GenericTagQuery.class);
    }

    @Override
    public void serialize(GenericTagQuery value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        var mapper = IEncoder.MAPPER;
        JsonNode node = mapper.valueToTree(value);
        ObjectNode objNode = (ObjectNode) node;
        String attrName = "#" + value.getTagName();
        objNode.set(attrName, node.get("value"));
        objNode.remove("tagName");
        objNode.remove("value");

        gen.writeTree(objNode);
    }
}
