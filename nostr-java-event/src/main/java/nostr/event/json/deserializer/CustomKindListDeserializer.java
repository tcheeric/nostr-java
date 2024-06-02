package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.Kind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomKindListDeserializer extends JsonDeserializer<List<Kind>> {

    @Override
    public List<Kind> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        List<Kind> kindList = new ArrayList<>();
        JsonNode node = jsonParser.readValueAsTree();
        if (node.isArray()) {
            for (JsonNode n : node) {
                kindList.add(Kind.valueOf(Integer.decode(n.asText())));
            }
        }
        return kindList;
    }
}
