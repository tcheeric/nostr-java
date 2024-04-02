package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.list.KindList;

import java.io.IOException;

public class CustomKindListDeserializer extends JsonDeserializer<KindList> {
    @Override
    public KindList deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        KindList kindList = new KindList();
        JsonNode node = jsonParser.readValueAsTree();
        if (node.isArray()) {
            for (JsonNode n : node) {
                kindList.add(Integer.decode(n.asText()));
            }
        }
        return kindList;
    }
}
