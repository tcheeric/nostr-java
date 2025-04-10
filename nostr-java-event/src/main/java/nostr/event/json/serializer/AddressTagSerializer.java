package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nostr.event.tag.AddressTag;

/**
 *
 * @author eric
 */
public class AddressTagSerializer extends JsonSerializer<AddressTag> {

    @Override
    public void serialize(AddressTag addressTag, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString("a");
        
        jsonGenerator.writeString(
            Stream.of(
                    addressTag.getKind(),
                    addressTag.getPublicKey().toHexString(),
                    addressTag.getIdentifierTag().getUuid())
                .map(Object::toString).collect(Collectors.joining(":")));

        if (addressTag.getRelay() != null) {
            jsonGenerator.writeString("," + addressTag.getRelay().getUri());
        }
        jsonGenerator.writeEndArray();
    }

}
