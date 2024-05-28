package nostr.event.json.serializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import nostr.base.IEncoder;
import nostr.event.BaseEvent;
import nostr.event.list.BaseList;

/**
 * @author guilhermegps
 *
 */
public class CustomBaseListSerializer<T extends BaseList<U>, U extends BaseEvent> extends JsonSerializer<T> {

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) {
        try {
            var list = value.getList().parallelStream().map(this::toJson)
                    .collect(Collectors.toList());

            gen.writePOJO(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode toJson(Object obj) {
        var mapper = IEncoder.MAPPER;
        try {
            JsonNode node = mapper.valueToTree(obj);

            if (node.isObject()) {
                Iterator<Entry<String, JsonNode>> fields = node.fields();

                var list = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED), false)
                        .map(f -> f.getValue().asText().toLowerCase())
                        .collect(Collectors.toList());

                return mapper.valueToTree(list);
            }

            return node;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

}
