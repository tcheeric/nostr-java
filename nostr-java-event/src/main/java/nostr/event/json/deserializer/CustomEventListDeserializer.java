package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.list.EventList;

import java.io.IOException;

public class CustomEventListDeserializer extends JsonDeserializer<EventList> {

    @Override
    public EventList deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        EventList eventList = new EventList();
        JsonNode node = jsonParser.readValueAsTree();
        if (node.isArray()) {
            for (JsonNode n : node) {
                GenericEventDecoder decoder = new GenericEventDecoder(n.toString());
                eventList.add(decoder.decode());
            }
        }
        return eventList;
    }
}
