package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.base.IDecoder;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.stream.IntStream;

@Data
@Slf4j
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
            String[] jsonElements = I_DECODER_MAPPER_AFTERBURNER.readValue(json, String[].class);
            GenericTag genericTag = new GenericTag(
                    jsonElements[0],
                    new ArrayList<>() {
                        {
                            for (int i = 1; i < jsonElements.length; i++) {
                                ElementAttribute attribute = new ElementAttribute(
                                        "param" + (i - 1),
                                        jsonElements[i]);
                                if (!contains(attribute)) {
                                    add(attribute);
                                }
                            }
                        }
                    });
            /*
            GenericTag genericTag = new GenericTag(
                    jsonElements[0],
                    IntStream.of(1, jsonElements.length - 1)
                            .mapToObj(i ->
                                    new ElementAttribute(
                                            "param".concat(String.valueOf(i - 1)),
                                            jsonElements[i]))
                            .distinct()
                            .toList());
*/

            log.info("Decoded GenericTag: {}", genericTag);

            return (T) genericTag;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
