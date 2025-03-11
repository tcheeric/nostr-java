package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.BaseEvent;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static nostr.base.IEncoder.I_ENCODER_MAPPER_AFTERBURNER;

/**
 * @author guilhermegps
 *
 */
public class CustomBaseListSerializer<T extends List<U>, U extends BaseEvent> extends JsonSerializer<T> {

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            var list = value.parallelStream().map(this::toJson).toList();

            gen.writePOJO(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode toJson(Object obj) {
        var mapper = I_ENCODER_MAPPER_AFTERBURNER;
        try {
            JsonNode node = mapper.valueToTree(obj);

            if (node.isObject()) {
                Iterator<Entry<String, JsonNode>> fields = node.fields();

                var list = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED), false)
                    .map(f -> f.getValue().asText().toLowerCase()).toList();

                return mapper.valueToTree(list);
            }

            return node;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

}
