package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.GenericEventDecoder;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@NoArgsConstructor
public class CustomEventListDeserializer<T extends List<U>, U extends GenericEvent> extends JsonDeserializer<T> {

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        List<U> eventList = new ArrayList<>();
        JsonNode node = jsonParser.readValueAsTree();
        if (node.isArray()) {
            for (JsonNode n : node) {
                eventList.add(new GenericEventDecoder<U>().decode(n.toString()));
            }
        }
        return (T) eventList;
    }
}
