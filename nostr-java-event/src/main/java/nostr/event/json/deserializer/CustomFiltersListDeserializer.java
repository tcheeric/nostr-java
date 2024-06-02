package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.event.impl.Filters;
import nostr.event.json.codec.FiltersDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@NoArgsConstructor
public class CustomFiltersListDeserializer extends JsonDeserializer<List<Filters>> {
    @Override
    public List<Filters> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.readValueAsTree();
        return parseJson(node.toString());
    }

    public List<Filters> parseJson(@NonNull String jsonString) throws IOException {
        if (!jsonString.startsWith("[")) {
            jsonString = "[" + jsonString.trim();
        }
        if (!jsonString.endsWith("]")) {
            jsonString = jsonString + "]";
        }

        List<Filters> filtersList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);
        Iterator<JsonNode> elementsIterator = rootNode.elements();
        while (elementsIterator.hasNext()) {
            JsonNode element = elementsIterator.next();
            String strFilters = element.toString();
            FiltersDecoder decoder = new FiltersDecoder(strFilters);
            filtersList.add(decoder.decode());
        }

        return filtersList;
    }
}
