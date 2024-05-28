package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import nostr.event.impl.Filters;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.list.FiltersList;

import java.io.IOException;
import java.util.Iterator;

public class CustomFiltersListDeserializer<T extends FiltersList<U>, U extends Filters> extends JsonDeserializer<T> {
    private final Class<U> clazz;

    public CustomFiltersListDeserializer() {
        this.clazz = (Class<U>) Filters.class;
    }

  @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.readValueAsTree();
        return parseJson(node.toString());
    }

    public T parseJson(@NonNull String jsonString) throws IOException {
        if (!jsonString.startsWith("[")) {
            jsonString = "[" + jsonString.trim();
        }
        if (!jsonString.endsWith("]")) {
            jsonString = jsonString + "]";
        }

        FiltersList<U> filtersList = new FiltersList<>(clazz);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);
        Iterator<JsonNode> elementsIterator = rootNode.elements();
        while (elementsIterator.hasNext()) {
            JsonNode element = elementsIterator.next();
            String strFilters = element.toString();
            FiltersDecoder<U> decoder = new FiltersDecoder<>(strFilters);
            filtersList.add(decoder.decode());
        }

        return (T) filtersList;
    }
}
