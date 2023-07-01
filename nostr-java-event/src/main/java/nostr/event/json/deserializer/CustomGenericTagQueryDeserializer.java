package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nostr.base.GenericTagQuery;

public class CustomGenericTagQueryDeserializer extends JsonDeserializer<GenericTagQuery> {

    @Override
    public GenericTagQuery deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

        var genericTagQuery = new GenericTagQuery();

        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        if (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String tagName = field.getKey();
            JsonNode valuesNode = field.getValue();
            List<String> values = objectMapper.convertValue(valuesNode, ArrayList.class);
            genericTagQuery.setTagName(tagName.charAt(1)); // Assuming tagName is always a single character preceded by '#'
            genericTagQuery.setValue(values);
        }

        return genericTagQuery;
    }
}
