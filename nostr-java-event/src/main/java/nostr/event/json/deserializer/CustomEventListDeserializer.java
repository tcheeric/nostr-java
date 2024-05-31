package nostr.event.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.list.EventList;

import java.io.IOException;

public class CustomEventListDeserializer<T extends EventList<U>, U extends GenericEvent> extends JsonDeserializer<T> {
    private final Class<U> clazz;

    public CustomEventListDeserializer() {
        this.clazz = (Class<U>) GenericEvent.class;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        EventList<U> eventList = new EventList<>(clazz);
        JsonNode node = jsonParser.readValueAsTree();
        if (node.isArray()) {
            for (JsonNode n : node) {
                GenericEventDecoder<U> decoder = new GenericEventDecoder<>(n.toString());
                eventList.add(decoder.decode());
            }
        }
        return (T) eventList;
    }
}
