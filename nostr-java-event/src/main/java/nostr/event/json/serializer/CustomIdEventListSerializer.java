package nostr.event.json.serializer;

import java.io.IOException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import nostr.event.impl.GenericEvent;
import nostr.event.list.EventList;

/**
 * @author guilhermegps
 *
 */
public class CustomIdEventListSerializer extends JsonSerializer<EventList> {

    @Override
    public void serialize(EventList value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            var list = value.getList().parallelStream().map(GenericEvent::getId).collect(Collectors.toList());

            gen.writePOJO(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
