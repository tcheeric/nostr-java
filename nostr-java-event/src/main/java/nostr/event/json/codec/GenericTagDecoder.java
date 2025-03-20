package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.IDecoder;
import nostr.event.impl.GenericTag;

import java.util.stream.IntStream;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

@Data
public class GenericTagDecoder<T extends GenericTag> implements IDecoder<T> {

    private final Class<T> clazz;

    public GenericTagDecoder() {
        this((Class<T>) GenericTag.class);
    }

    public GenericTagDecoder(@NonNull Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T decode(@NonNull String json) {
        try {
            String[] jsonElements = MAPPER_AFTERBURNER.readValue(json, String[].class);
            GenericTag genericTag = new GenericTag(
                jsonElements[0], // value at index 0 designated as generic tag's "code"
                IntStream.of(0, jsonElements.length)
                    .mapToObj(i ->
                        new ElementAttribute(
                            "param".concat(String.valueOf(i)),
                            jsonElements[i]))
                    .toList());
            return (T) genericTag;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
